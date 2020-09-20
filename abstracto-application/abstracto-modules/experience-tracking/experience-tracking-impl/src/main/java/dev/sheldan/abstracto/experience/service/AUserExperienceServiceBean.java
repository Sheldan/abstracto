package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeatureConfig;
import dev.sheldan.abstracto.experience.models.ExperienceGainResult;
import dev.sheldan.abstracto.experience.models.RoleCalculationResult;
import dev.sheldan.abstracto.experience.models.database.*;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.templates.UserSyncStatusModel;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AUserExperienceServiceBean implements AUserExperienceService {

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Autowired
    private ExperienceRoleService experienceRoleService;

    @Autowired
    private ExperienceLevelManagementService experienceLevelManagementService;

    @Autowired
    private ExperienceRoleManagementService experienceRoleManagementService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private DisabledExpRoleManagementService disabledExpRoleManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private BotService botService;

    @Autowired
    private RunTimeExperienceService runTimeExperienceService;

    @Autowired
    private AUserExperienceServiceBean self;

    /**
     * Creates the user in the runtime experience, if the user was not in yet. Also creates an entry for the minute, if necessary.
     * @param userInAServer The {@link AUserInAServer} to be added to the list of users gaining experience
     */
    @Override
    public void addExperience(AUserInAServer userInAServer) {
        Long second = Instant.now().getEpochSecond() / 60;
        Map<Long, List<AServer>> runtimeExperience = runTimeExperienceService.getRuntimeExperience();
        if(runtimeExperience.containsKey(second)) {
            List<AServer> existing = runtimeExperience.get(second);
            existing.forEach(server -> {
                if(server.getUsers().stream().noneMatch(userInAServer1 -> userInAServer.getUserInServerId().equals(userInAServer1.getUserInServerId()))) {
                    server.getUsers().add(userInAServer);
                }
            });

        } else {
            AServer server = AServer
                    .builder()
                    .id(userInAServer.getServerReference().getId())
                    .build();
            server.getUsers().add(userInAServer);
            runtimeExperience.put(second, Arrays.asList(server));
        }
    }

    @Override
    public Map<Long, List<AServer>> getRuntimeExperience() {
        return runTimeExperienceService.getRuntimeExperience();
    }


    /**
     * Calculates the level of the given {@link AUserExperience} according to the given {@link AExperienceLevel} list
     * @param levels The list of {@link AExperienceLevel} representing the level configuration, this must include the initial level 0
     *      *               This level will be taken as the initial value, and if no other level qualifies, this will be taken.
     *               The levels must be ordered.
     * @param experienceCount
     * @return The appropriate level according to the level config
     */
    @Override
    public AExperienceLevel calculateLevel(List<AExperienceLevel> levels, Long experienceCount) {
        AExperienceLevel lastLevel = levels.get(0);
        for (AExperienceLevel level : levels) {
            if(level.getExperienceNeeded() >= experienceCount) {
                return lastLevel;
            } else {
                lastLevel = level;
            }
        }
        return lastLevel;
    }

    @Override
    public boolean updateUserLevel(AUserExperience userExperience, List<AExperienceLevel> levels, Long experienceCount) {
        AUserInAServer user = userExperience.getUser();
        AExperienceLevel correctLevel = calculateLevel(levels, experienceCount);
        Integer currentLevel = userExperience.getCurrentLevel() != null ? userExperience.getCurrentLevel().getLevel() : 0;
        if(!correctLevel.getLevel().equals(currentLevel)) {
            log.info("User {} leveled from {} to {}", user.getUserReference().getId(), currentLevel, correctLevel.getLevel());
            userExperience.setCurrentLevel(correctLevel);
            return true;
        }
        return false;
    }

    /**
     * Calculates the actually gained experience for every user in the given servers and adds them to the users.
     * This method only actually increases the message count, and calls other methods for experience gain
     * and role change.
     * Loads the level and role configuration for each server and sorts them for them to be used.
     * Only actually updates the role, if the user also changed level.
     * @param servers The list of {@link AServer} containing the user which need to gain experience
     */
    @Transactional
    @Override
    public CompletableFuture<Void> handleExperienceGain(List<AServer> servers) {
        List<ExperienceGainResult> resultFutures = new ArrayList<>();
        List<CompletableFuture<RoleCalculationResult>> futures = new ArrayList<>();
        List<AExperienceLevel> levels = experienceLevelManagementService.getLevelConfig();
        // TODO what if there are a lot in here...., transaction size etc
        servers.forEach(serverExp -> {
            log.trace("Handling experience for server {}", serverExp.getId());
            int minExp = configService.getLongValue(ExperienceFeatureConfig.MIN_EXP_KEY, serverExp.getId()).intValue();
            int maxExp = configService.getLongValue(ExperienceFeatureConfig.MAX_EXP_KEY, serverExp.getId()).intValue();
            Double multiplier = configService.getDoubleValue(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, serverExp.getId());
            PrimitiveIterator.OfInt iterator = new Random().ints(serverExp.getUsers().size(), minExp, maxExp + 1).iterator();
            levels.sort(Comparator.comparing(AExperienceLevel::getExperienceNeeded));
            List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(serverExp);
            List<ADisabledExpRole> disabledExpRoles = disabledExpRoleManagementService.getDisabledRolesForServer(serverExp);
            List<ARole> disabledRoles = disabledExpRoles.stream().map(ADisabledExpRole::getRole).collect(Collectors.toList());
            roles.sort(Comparator.comparing(role -> role.getLevel().getLevel()));
            serverExp.getUsers().forEach(userInAServer -> {
                Integer gainedExperience = iterator.next();
                gainedExperience = (int) Math.floor(gainedExperience * multiplier);
                Member member = botService.getMemberInServer(userInAServer);
                if(!roleService.hasAnyOfTheRoles(member, disabledRoles)) {
                    log.trace("Handling {}. The user gains {}", userInAServer.getUserReference().getId(), gainedExperience);
                    AUserExperience aUserExperience = userExperienceManagementService.findUserInServer(userInAServer);
                    if(Boolean.FALSE.equals(aUserExperience.getExperienceGainDisabled())) {
                        Long newExperienceCount = aUserExperience.getExperience() + gainedExperience.longValue();
                        AExperienceLevel newLevel = calculateLevel(levels, newExperienceCount);
                        CompletableFuture<RoleCalculationResult> resultFuture = updateUserRole(aUserExperience, roles, newLevel.getLevel());
                        Long newMessageCount = aUserExperience.getMessageCount() + 1L;
                        ExperienceGainResult calculationResult =
                                ExperienceGainResult
                                    .builder()
                                        .calculationResult(resultFuture)
                                        .newExperience(newExperienceCount)
                                        .newMessageCount(newMessageCount)
                                        .newLevel(newLevel.getLevel())
                                        .userInServerId(userInAServer.getUserInServerId())
                                    .build();
                        resultFutures.add(calculationResult);
                        futures.add(resultFuture);
                    } else {
                        log.trace("Experience gain was disabled. User did not gain any experience.");
                    }
                } else {
                    log.trace("User {} has a role which makes the user unable to gain experience.", userInAServer.getUserInServerId());
                }
            });
        });

        return FutureUtils.toSingleFutureGeneric(futures).thenAccept(aVoid ->
           self.persistExperienceChanges(resultFutures)
        );
    }

    @Transactional
    public void persistExperienceChanges(List<ExperienceGainResult> resultFutures) {
        List<AExperienceLevel> levels = experienceLevelManagementService.getLevelConfig();
        HashMap<Long, List<AExperienceRole>> serverRoleMapping = new HashMap<>();
        resultFutures.forEach(experienceGainResult -> {
            AUserInAServer user = userInServerManagementService.loadUser(experienceGainResult.getUserInServerId());
            AUserExperience userExperience = userExperienceManagementService.findUserInServer(user);
            userExperience.setMessageCount(experienceGainResult.getNewMessageCount());
            userExperience.setExperience(experienceGainResult.getNewExperience());
            Optional<AExperienceLevel> foundLevel = levels.stream().filter(level -> level.getLevel().equals(experienceGainResult.getNewLevel())).findFirst();
            if(foundLevel.isPresent()) {
                userExperience.setCurrentLevel(foundLevel.get());
            } else {
                log.warn("User {} was present, but no level could be found.", userExperience.getUser().getUserReference().getId());
            }
            AServer server = user.getServerReference();
            if(!serverRoleMapping.containsKey(server.getId())) {
                serverRoleMapping.put(server.getId(), experienceRoleManagementService.getExperienceRolesForServer(server));
            }
            List<AExperienceRole> roleConfig = serverRoleMapping.get(server.getId());
            AExperienceRole role = experienceRoleService.calculateRole(roleConfig, userExperience.getLevelOrDefault());
            userExperience.setCurrentExperienceRole(role);
        });
    }

    /**
     * Calculates the appropriate level of the user and changes the role, if the {@link AExperienceLevel} changes.
     * This changes the config in the database, and also gives the {@link net.dv8tion.jda.api.entities.Member} the new
     * {@link net.dv8tion.jda.api.entities.Role}. If the user does not warrant an {@link AExperienceRole},
     * this method also removes it. The role is only changed, if the user does not have
     * @param userExperience The {@link AUserExperience} object to recalculate the {@link AExperienceRole} for
     * @param roles The list of {@link AExperienceRole} used as a role configuration
     */
    @Override
    public CompletableFuture<RoleCalculationResult> updateUserRole(AUserExperience userExperience, List<AExperienceRole> roles, Integer currentLevel) {
        AUserInAServer user = userExperience.getUser();
        Function<Void, RoleCalculationResult> returnNullRole = aVoid -> RoleCalculationResult
                .builder()
                .userInServerId(user.getUserInServerId())
                .experienceRoleId(null)
                .build();
        if(!botService.isUserInGuild(userExperience.getUser())) {
            log.trace("User {} is not in server {} anymore. No role calculation done.", userExperience.getUser().getUserInServerId(), userExperience.getUser().getServerReference().getId());
            return CompletableFuture.completedFuture(returnNullRole.apply(null));
        }
        Long userInServerId = user.getUserInServerId();
        log.trace("Updating experience role for user {} in server {}", user.getUserReference().getId(), user.getServerReference().getId());
        AExperienceRole role = experienceRoleService.calculateRole(roles, currentLevel);
        Member member = botService.getMemberInServer(user.getServerReference(), user.getUserReference());
        boolean currentlyHasNoExperienceRole = userExperience.getCurrentExperienceRole() == null;
        if(role == null) {
            if(!currentlyHasNoExperienceRole){
                return roleService.removeRoleFromUserFuture(user, userExperience.getCurrentExperienceRole().getRole())
                        .thenApply(returnNullRole);
            }
            return CompletableFuture.completedFuture(returnNullRole.apply(null));
        }
        boolean userHasRoleAlready = roleService.memberHasRole(member, role.getRole());
        Long experienceRoleId = role.getId();
        Function<Void, RoleCalculationResult> fullResult = aVoid -> RoleCalculationResult
                .builder()
                .experienceRoleId(experienceRoleId)
                .userInServerId(userInServerId)
                .build();
        if(!userHasRoleAlready && (currentlyHasNoExperienceRole || !role.getRole().getId().equals(userExperience.getCurrentExperienceRole().getRole().getId()))) {
            log.info("User {} in server {} gets a new role {}", user.getUserReference().getId(), user.getServerReference().getId(), role.getRole().getId());
            CompletableFuture<Void> removalFuture;
            if(!currentlyHasNoExperienceRole && botService.isUserInGuild(userExperience.getUser())) {
                removalFuture = roleService.removeRoleFromUserFuture(user, userExperience.getCurrentExperienceRole().getRole());
            } else {
                removalFuture = CompletableFuture.completedFuture(null);
            }
            CompletableFuture<Void> addRoleFuture = roleService.addRoleToUserFuture(user, role.getRole());
            return CompletableFuture.allOf(removalFuture, addRoleFuture).thenApply(fullResult);
        }
        return CompletableFuture.completedFuture(fullResult.apply(null));
    }

    /**
     * Synchronizes the {@link net.dv8tion.jda.api.entities.Role} of all {@link net.dv8tion.jda.api.entities.Member} in
     * the given {@link AServer}. This might take a long time to complete, because there are a lot of role changes.
     * @param server The {@link AServer} to update the users for
     */
    @Override
    public List<CompletableFuture<RoleCalculationResult>> syncUserRoles(AServer server) {
        List<CompletableFuture<RoleCalculationResult>> results = new ArrayList<>();
        List<AUserExperience> aUserExperiences = userExperienceManagementService.loadAllUsers(server);
        log.info("Found {} users to synchronize", aUserExperiences.size());
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(server);
        for (int i = 0; i < aUserExperiences.size(); i++) {
            AUserExperience userExperience = aUserExperiences.get(i);
            log.trace("Synchronizing {} out of {}", i, aUserExperiences.size());
            results.add(updateUserRole(userExperience, roles, userExperience.getCurrentLevel().getLevel()));
        }
        return results;
    }

    /**
     * Synchronizes the roles of all the users and provides feedback to the user executing
     * @param server The {@link AServer} to update users for
     * @param channel The {@link AChannel} in which the {@link dev.sheldan.abstracto.experience.models.templates.UserSyncStatusModel}
     */
    @Override
    public CompletableFuture<Void> syncUserRolesWithFeedback(AServer server, AChannel channel) {
        List<AUserExperience> aUserExperiences = userExperienceManagementService.loadAllUsers(server);
        log.info("Found {} users to synchronize", aUserExperiences.size());
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(server);
        CompletableFutureList<RoleCalculationResult> calculations = executeActionOnUserExperiencesWithFeedBack(aUserExperiences, channel, (AUserExperience experience) -> updateUserRole(experience, roles, experience.getLevelOrDefault()));
        return calculations.getMainFuture().thenAccept(aVoid ->
            self.syncRolesInStorage(calculations.getObjects())
        );
    }

    /**
     * Updates the actually stored experience roles in the database
     * @param results The list of {@link RoleCalculationResult} which should be applied
     */
    @Transactional
    public void syncRolesInStorage(List<RoleCalculationResult> results) {
        results.forEach(result -> {
            if(result != null) {
                AUserInAServer user = userInServerManagementService.loadUser(result.getUserInServerId());
                AUserExperience userExperience = userExperienceManagementService.findUserInServer(user);
                log.trace("Updating experience role for {} in server {} to {}", user.getUserInServerId(), user.getServerReference(), result.getExperienceRoleId());
                if(result.getExperienceRoleId() != null) {
                    AExperienceRole role = experienceRoleManagementService.getExperienceRoleById(result.getExperienceRoleId());
                    userExperience.setCurrentExperienceRole(role);
                } else {
                    userExperience.setCurrentExperienceRole(null);
                }
            }
        });
    }

    /**
     * Executes the given {@link Consumer} on each of the experiences and provides feedback in the given AChannel in the form of a status message
     * @param experiences The list of {@link AUserExperience} to be working on
     * @param channel The {@link AChannel} used to provide feedback to the user
     * @param toExecute The {@link Consumer} which should be executed on each element of the passed list
     */
    @Override
    public CompletableFutureList<RoleCalculationResult> executeActionOnUserExperiencesWithFeedBack(List<AUserExperience> experiences, AChannel channel, Function<AUserExperience, CompletableFuture<RoleCalculationResult>> toExecute) {
        List<CompletableFuture<RoleCalculationResult>> futures = new ArrayList<>();
        MessageToSend status = getUserSyncStatusUpdateModel(0, experiences.size());
        try {
            Message statusMessage = messageService.createStatusMessage(status, channel).get();
            int interval = Math.min(Math.max(experiences.size() / 10, 1), 100);
            for (int i = 0; i < experiences.size(); i++) {
                if((i % interval) == 1) {
                    log.trace("Updating feedback message with new index {} out of {}", i, experiences.size());
                    status = getUserSyncStatusUpdateModel(i, experiences.size());
                    messageService.updateStatusMessage(channel, statusMessage.getIdLong(), status);
                }
                futures.add(toExecute.apply(experiences.get(i)));
                log.trace("Synchronizing {} out of {}", i, experiences.size());
            }
            status = getUserSyncStatusUpdateModel(experiences.size(), experiences.size());
            messageService.updateStatusMessage(channel, statusMessage.getIdLong(), status);
        } catch (InterruptedException | ExecutionException e) {
            log.info("Failed to synchronize users.", e);
            Thread.currentThread().interrupt();
        }

        return new CompletableFutureList<>(futures);
    }

    @Override
    public void disableExperienceForUser(AUserInAServer userInAServer) {
        AUserExperience userExperience = userExperienceManagementService.findUserInServer(userInAServer);
        userExperience.setExperienceGainDisabled(true);
    }

    @Override
    public void enableExperienceForUser(AUserInAServer userInAServer) {
        AUserExperience userExperience = userExperienceManagementService.findUserInServer(userInAServer);
        userExperience.setExperienceGainDisabled(false);
    }

    private MessageToSend getUserSyncStatusUpdateModel(Integer current, Integer total) {
        UserSyncStatusModel statusModel = UserSyncStatusModel.builder().currentCount(current).totalUserCount(total).build();
        return templateService.renderEmbedTemplate("user_sync_status_message", statusModel);
    }

    /**
     * Retrieves the role configuration and executes the method responsible to sync the experience role of the user
     * @param userExperience The {@link AUserExperience} to synchronize the role for
     */
    @Override
    public CompletableFuture<RoleCalculationResult> syncForSingleUser(AUserExperience userExperience) {
        AUserInAServer user = userExperience.getUser();
        log.info("Synchronizing for user {} in server {}", user.getUserReference().getId(), user.getServerReference().getId());
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(user.getServerReference());
        return updateUserRole(userExperience, roles, userExperience.getLevelOrDefault());
    }

    /**
     * Retrieves the leader board data for the given page of the given server
     * @param server The {@link AServer} to retrieve the leader board for
     * @param page The desired page on the leader board. The page size is 10
     * @return The {@link LeaderBoard} containing all necessary information concerning the leader board
     */
    @Override
    public LeaderBoard findLeaderBoardData(AServer server, Integer page) {
        if(page <= 0) {
            throw new IllegalArgumentException("Page needs to be >= 1");
        }
        page--;
        int pageSize = 10;
        List<AUserExperience> experiences = userExperienceManagementService.findLeaderBoardUsersPaginated(server, page * pageSize, (page + 1) * pageSize);
        List<LeaderBoardEntry> entries = new ArrayList<>();
        for (int i = 0; i < experiences.size(); i++) {
            AUserExperience userExperience = experiences.get(i);
            entries.add(LeaderBoardEntry.builder().experience(userExperience).rank((page * pageSize) + i + 1).build());
        }
        return LeaderBoard.builder().entries(entries).build();
    }

    /**
     * Builds an {@link AUserExperience} and loads the appropriate rank of the passed {@link AUserInAServer}
     * @param userInAServer The {@link AUserInAServer} to retrieve the {@link LeaderBoardEntry} for
     * @return The {@link LeaderBoardEntry} representing one single row in the leader board
     */
    @Override
    public LeaderBoardEntry getRankOfUserInServer(AUserInAServer userInAServer) {
        log.info("Retrieving rank for {}", userInAServer.getUserReference().getId());
        AUserExperience aUserExperience = userExperienceManagementService.findUserInServer(userInAServer);
        Integer rank = 0;
        if(aUserExperience != null) {
            LeaderBoardEntryResult rankOfUserInServer = userExperienceManagementService.getRankOfUserInServer(aUserExperience);
            if(rankOfUserInServer != null) {
                rank = rankOfUserInServer.getRank();
            }
        }
        return LeaderBoardEntry.builder().experience(aUserExperience).rank(rank).build();
    }

}

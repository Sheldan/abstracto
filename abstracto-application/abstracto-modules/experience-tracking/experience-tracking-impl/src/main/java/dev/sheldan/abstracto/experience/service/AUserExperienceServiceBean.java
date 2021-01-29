package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeatureConfig;
import dev.sheldan.abstracto.experience.models.*;
import dev.sheldan.abstracto.experience.models.database.*;
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
    private MemberService memberService;

    @Autowired
    private RunTimeExperienceService runTimeExperienceService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private AUserExperienceServiceBean self;

    /**
     * Creates the user in the runtime experience, if the user was not in yet. Also creates an entry for the minute, if necessary.
     * @param userInAServer The {@link AUserInAServer} to be added to the list of users gaining experience
     */
    @Override
    public void addExperience(AUserInAServer userInAServer) {
        runTimeExperienceService.takeLock();
        try {
            Long minute = Instant.now().getEpochSecond() / 60;
            Map<Long, List<ServerExperience>> runtimeExperience = runTimeExperienceService.getRuntimeExperience();
            if(runtimeExperience.containsKey(minute)) {
                log.trace("Minute {} already tracked, adding user {} in server {}.",
                        minute, userInAServer.getUserReference().getId(), userInAServer.getServerReference().getId());
                List<ServerExperience> existing = runtimeExperience.get(minute);
                for (ServerExperience server : existing) {
                    if (server.getServerId().equals(userInAServer.getServerReference().getId()) && server.getUserInServerIds().stream().noneMatch(userInAServer1 -> userInAServer.getUserInServerId().equals(userInAServer1))) {
                        server.getUserInServerIds().add(userInAServer.getUserInServerId());
                        break;
                    }
                }

            } else {
                log.trace("Minute {} did not exist yet. Creating new entry for user {} in server {}.", minute, userInAServer.getUserReference().getId(), userInAServer.getServerReference().getId());
                ServerExperience serverExperience = ServerExperience
                        .builder()
                        .serverId(userInAServer.getServerReference().getId())
                        .build();
                serverExperience.getUserInServerIds().add(userInAServer.getUserInServerId());
                runtimeExperience.put(minute, new ArrayList<>(Arrays.asList(serverExperience)));
            }
        } finally {
            runTimeExperienceService.releaseLock();
        }
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
                log.trace("Calculated level {} for {} experience.", lastLevel.getLevel(), experienceCount);
                return lastLevel;
            } else {
                lastLevel = level;
            }
        }
        log.trace("Calculated level {} for {} experience.", lastLevel.getLevel(), experienceCount);
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
    public CompletableFuture<Void> handleExperienceGain(List<ServerExperience> servers) {
        List<ExperienceGainResult> resultFutures = new ArrayList<>();
        List<CompletableFuture<RoleCalculationResult>> futures = new ArrayList<>();
        List<AExperienceLevel> levels = experienceLevelManagementService.getLevelConfig();
        // TODO what if there are a lot in here...., transaction size etc
        servers.forEach(serverExp -> {
            AServer server = serverManagementService.loadOrCreate(serverExp.getServerId());
            log.info("Handling {} experience for server {}", serverExp.getUserInServerIds().size(), serverExp.getServerId());
            int minExp = configService.getLongValue(ExperienceFeatureConfig.MIN_EXP_KEY, serverExp.getServerId()).intValue();
            int maxExp = configService.getLongValue(ExperienceFeatureConfig.MAX_EXP_KEY, serverExp.getServerId()).intValue();
            Double multiplier = configService.getDoubleValue(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, serverExp.getServerId());
            PrimitiveIterator.OfInt iterator = new Random().ints(serverExp.getUserInServerIds().size(), minExp, maxExp + 1).iterator();
            levels.sort(Comparator.comparing(AExperienceLevel::getExperienceNeeded));
            List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(server);
            List<ADisabledExpRole> disabledExpRoles = disabledExpRoleManagementService.getDisabledRolesForServer(server);
            List<ARole> disabledRoles = disabledExpRoles.stream().map(ADisabledExpRole::getRole).collect(Collectors.toList());
            roles.sort(Comparator.comparing(role -> role.getLevel().getLevel()));
            serverExp.getUserInServerIds().forEach(userInAServerId -> {
                Integer gainedExperience = iterator.next();
                AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(userInAServerId);
                gainedExperience = (int) Math.floor(gainedExperience * multiplier);
                Member member = memberService.getMemberInServer(userInAServer);
                if(member != null && !roleService.hasAnyOfTheRoles(member, disabledRoles)) {
                    log.trace("Handling {}. The user gains {}", userInAServer.getUserReference().getId(), gainedExperience);
                    Optional<AUserExperience> aUserExperienceOptional = userExperienceManagementService.findByUserInServerIdOptional(userInAServerId);
                    if(aUserExperienceOptional.isPresent()) {
                        AUserExperience aUserExperience = aUserExperienceOptional.get();
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
                        log.info("User experience for user {} was not found. Planning to create new instance.", userInAServer.getUserInServerId());
                        Long newExperience = gainedExperience.longValue();
                        AExperienceLevel newLevel = calculateLevel(levels, newExperience);
                        Long newMessageCount = 1L;
                        CompletableFuture<RoleCalculationResult> resultFuture = applyInitialRole(userInAServer, roles, newLevel.getLevel());
                        ExperienceGainResult calculationResult =
                                ExperienceGainResult
                                        .builder()
                                        .calculationResult(resultFuture)
                                        .newExperience(newExperience)
                                        .newMessageCount(newMessageCount)
                                        .newLevel(newLevel.getLevel())
                                        .createUserExperience(true)
                                        .userInServerId(userInAServer.getUserInServerId())
                                        .build();
                        resultFutures.add(calculationResult);
                        futures.add(resultFuture);
                    }
                } else {
                    log.trace("User {} has a role which makes the user unable to gain experience or the member could not be found in the server.", userInAServer.getUserInServerId());
                }
            });
        });

        return FutureUtils.toSingleFutureGeneric(futures).thenAccept(aVoid ->
           self.persistExperienceChanges(resultFutures)
        );
    }

    private CompletableFuture<RoleCalculationResult> applyInitialRole(AUserInAServer aUserInAServer, List<AExperienceRole> roles, Integer currentLevel) {
        if(!memberService.isUserInGuild(aUserInAServer)) {
            log.trace("User {} is not in server {} anymore. No role calculation done.", aUserInAServer.getUserInServerId(), aUserInAServer.getServerReference().getId());
            return CompletableFuture.completedFuture(RoleCalculationResult
                    .builder()
                    .userInServerId(aUserInAServer.getUserInServerId())
                    .experienceRoleId(null)
                    .build());
        }
        AExperienceRole role = experienceRoleService.calculateRole(roles, currentLevel);
        if(role == null) {
            log.trace("No experience role calculated. Applying none to user {} in server {}.",
                    aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId());
            return CompletableFuture.completedFuture(RoleCalculationResult
                    .builder()
                    .userInServerId(aUserInAServer.getUserInServerId())
                    .experienceRoleId(null)
                    .build());
        }
        Long experienceRoleId = role.getId();
        Long userInServerId = aUserInAServer.getUserInServerId();
        log.trace("Applying {} as the first experience role for user {} in server {}.",
                experienceRoleId, aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId());
        return roleService.addRoleToUserFuture(aUserInAServer, role.getRole()).thenApply(aVoid -> RoleCalculationResult
                .builder()
                .experienceRoleId(experienceRoleId)
                .userInServerId(userInServerId)
                .build());
    }

    @Transactional
    public void persistExperienceChanges(List<ExperienceGainResult> resultFutures) {
        List<AExperienceLevel> levels = experienceLevelManagementService.getLevelConfig();
        log.info("Storing {} experience gain results.", resultFutures.size());
        HashMap<Long, List<AExperienceRole>> serverRoleMapping = new HashMap<>();
        resultFutures.forEach(experienceGainResult -> {
            AUserInAServer user = userInServerManagementService.loadOrCreateUser(experienceGainResult.getUserInServerId());
            AUserExperience userExperience;
            if(experienceGainResult.isCreateUserExperience()) {
                userExperience = userExperienceManagementService.createUserInServer(user);
                log.info("Creating new experience user {}", experienceGainResult.getUserInServerId());
            } else {
                userExperience = userExperienceManagementService.findByUserInServerId(experienceGainResult.getUserInServerId());
            }
            userExperience.setMessageCount(experienceGainResult.getNewMessageCount());
            userExperience.setExperience(experienceGainResult.getNewExperience());
            Optional<AExperienceLevel> foundLevel = levels.stream().filter(level -> level.getLevel().equals(experienceGainResult.getNewLevel())).findFirst();
            if(foundLevel.isPresent()) {
                userExperience.setCurrentLevel(foundLevel.get());
            } else {
                log.warn("User {} was present, but no level matching the calculation result {} could be found.", userExperience.getUser().getUserReference().getId(), experienceGainResult.getNewLevel());
            }
            AServer server = user.getServerReference();
            if(!serverRoleMapping.containsKey(server.getId())) {
                serverRoleMapping.put(server.getId(), experienceRoleManagementService.getExperienceRolesForServer(server));
            }
            RoleCalculationResult roleCalculationResult = experienceGainResult.getCalculationResult().join();
            if(roleCalculationResult.getExperienceRoleId() != null) {
                AExperienceRole role = experienceRoleManagementService.getExperienceRoleById(roleCalculationResult.getExperienceRoleId());
                userExperience.setCurrentExperienceRole(role);
            }
            if(experienceGainResult.isCreateUserExperience()) {
                userExperienceManagementService.saveUser(userExperience);
            }
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
        Long userInServerId = user.getUserInServerId();
        Long serverId = user.getServerReference().getId();
        log.trace("Updating experience role for user {} in server {}", user.getUserReference().getId(), serverId);
        AExperienceRole role = experienceRoleService.calculateRole(roles, currentLevel);
        boolean currentlyHasNoExperienceRole = userExperience.getCurrentExperienceRole() == null;
        // if calculation results in no role, do not add a role
        if(role == null) {
            log.trace("User {} in server {} does not have an experience role, according to new calculation.",
                    user.getUserReference().getId(), serverId);
            // if the user has a experience role currently, remove it
            if(!currentlyHasNoExperienceRole){
                return roleService.removeRoleFromUserFuture(user, userExperience.getCurrentExperienceRole().getRole())
                        .thenApply(returnNullRole);
            }
            return CompletableFuture.completedFuture(returnNullRole.apply(null));
        }
        Long experienceRoleId = role.getId();
        Long roleId = role.getRole().getId();
        // if the new role is already the one configured in the database
        Long userId = user.getUserReference().getId();
        Long oldUserExperienceRoleId = currentlyHasNoExperienceRole ? 0L : userExperience.getCurrentExperienceRole().getRole().getId();
        return memberService.getMemberInServerAsync(user).thenCompose(member -> {
            boolean userHasRoleAlready = roleService.memberHasRole(member, roleId);
            boolean userHasOldRole = false;
            boolean rolesChanged = true;
            if(!currentlyHasNoExperienceRole) {
                userHasOldRole = roleService.memberHasRole(member, oldUserExperienceRoleId);
                rolesChanged = !roleId.equals(oldUserExperienceRoleId);
            }
            Function<Void, RoleCalculationResult> fullResult = aVoid -> RoleCalculationResult
                    .builder()
                    .experienceRoleId(experienceRoleId)
                    .userInServerId(userInServerId)
                    .build();
            // if the roles changed or
            // the user does not have the new target role already
            // the user still has the old role
            if((!userHasRoleAlready || userHasOldRole)) {
                log.info("User {} in server {} gets a new role {} because of experience.", userId, serverId, roleId);
                CompletableFuture<Void> removalFuture;
                if(userHasOldRole && rolesChanged) {
                    removalFuture = roleService.removeRoleFromMemberAsync(member, oldUserExperienceRoleId);
                } else {
                    removalFuture = CompletableFuture.completedFuture(null);
                }
                CompletableFuture<Void> addRoleFuture;
                if(!userHasRoleAlready) {
                    addRoleFuture = roleService.addRoleToMemberFuture(member, roleId);
                } else {
                    addRoleFuture = CompletableFuture.completedFuture(null);
                }
                return CompletableFuture.allOf(removalFuture, addRoleFuture).thenApply(fullResult);
            }
            // we are turning the full calculation result regardless
            return CompletableFuture.completedFuture(fullResult.apply(null));
        });
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
     * @param channelId The ID of the channel in which the {@link dev.sheldan.abstracto.experience.models.templates.UserSyncStatusModel} should be posted to
     */
    @Override
    public CompletableFuture<Void> syncUserRolesWithFeedback(AServer server, Long channelId) {
        AChannel channel = channelManagementService.loadChannel(channelId);
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
                AUserInAServer user = userInServerManagementService.loadOrCreateUser(result.getUserInServerId());
                AUserExperience userExperience = userExperienceManagementService.findUserInServer(user);
                log.trace("Updating experience role for {} in server {} to {}", user.getUserInServerId(), user.getServerReference().getId(), result.getExperienceRoleId());
                if(result.getExperienceRoleId() != null) {
                    log.trace("User experience {} gets new experience role with id {}.", userExperience.getId(), result.getExperienceRoleId());
                    AExperienceRole role = experienceRoleManagementService.getExperienceRoleById(result.getExperienceRoleId());
                    userExperience.setCurrentExperienceRole(role);
                } else {
                    log.trace("User experience {} does not get a user experience role.", userExperience.getId());
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
        Message statusMessage = messageService.createStatusMessage(status, channel).join();
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

        return new CompletableFutureList<>(futures);
    }

    @Override
    public void disableExperienceForUser(AUserInAServer userInAServer) {
        log.info("Disabling experience gain for user {} in server {}.", userInAServer.getUserReference().getId(), userInAServer.getServerReference().getId());
        AUserExperience userExperience = userExperienceManagementService.findUserInServer(userInAServer);
        userExperience.setExperienceGainDisabled(true);
    }

    @Override
    public void enableExperienceForUser(AUserInAServer userInAServer) {
        AUserExperience userExperience = userExperienceManagementService.findUserInServer(userInAServer);
        log.info("Enabling experience gain for user {} in server {}.", userInAServer.getUserReference().getId(), userInAServer.getServerReference().getId());
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
        log.trace("Loading leaderboard page {} for server {}.", page, server.getId());
        List<AUserExperience> experiences = userExperienceManagementService.findLeaderBoardUsersPaginated(server, page * pageSize, (page + 1) * pageSize);
        List<LeaderBoardEntry> entries = new ArrayList<>();
        log.trace("Found {} experiences.", experiences.size());
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
        log.trace("Retrieving rank for {}", userInAServer.getUserReference().getId());
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

package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.models.property.SystemConfigProperty;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.*;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureConfig;
import dev.sheldan.abstracto.experience.exception.NoExperienceTrackedException;
import dev.sheldan.abstracto.experience.model.*;
import dev.sheldan.abstracto.experience.model.database.*;
import dev.sheldan.abstracto.experience.model.template.UserSyncStatusModel;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    private ChannelGroupService channelGroupService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Autowired
    private AUserExperienceServiceBean self;

    @Override
    public void addExperience(AUserInAServer userInAServer) {
        runTimeExperienceService.takeLock();
        try {
            Long minute = Instant.now().getEpochSecond() / 60;
            Map<Long, List<ServerExperience>> runtimeExperience = runTimeExperienceService.getRuntimeExperience();
            Long serverId = userInAServer.getServerReference().getId();
            Long userInServerId = userInAServer.getUserInServerId();
            if(runtimeExperience.containsKey(minute)) {
                log.debug("Minute {} already tracked, adding user {} in server {}.",
                        minute, userInAServer.getUserReference().getId(), serverId);
                List<ServerExperience> existing = runtimeExperience.get(minute);
                for (ServerExperience server : existing) {
                    if (server.getServerId().equals(serverId) && server.getUserInServerIds().stream().noneMatch(userInServerId::equals)) {
                        server.getUserInServerIds().add(userInServerId);
                        break;
                    }
                }

            } else {
                log.debug("Minute {} did not exist yet. Creating new entry for user {} in server {}.", minute, userInAServer.getUserReference().getId(), serverId);
                ServerExperience serverExperience = ServerExperience
                        .builder()
                        .serverId(serverId)
                        .build();
                serverExperience.getUserInServerIds().add(userInServerId);
                runtimeExperience.put(minute, new ArrayList<>(Arrays.asList(serverExperience)));
            }
        } finally {
            runTimeExperienceService.releaseLock();
        }
    }


    @Override
    public AExperienceLevel calculateLevel(List<AExperienceLevel> levels, Long experienceCount) {
        AExperienceLevel lastLevel = levels.get(0);
        for (AExperienceLevel level : levels) {
            if(level.getExperienceNeeded() >= experienceCount) {
                log.debug("Calculated level {} for {} experience.", lastLevel.getLevel(), experienceCount);
                return lastLevel;
            } else {
                lastLevel = level;
            }
        }
        log.debug("Calculated level {} for {} experience.", lastLevel.getLevel(), experienceCount);
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

    @Transactional
    @Override
    public CompletableFuture<Void> handleExperienceGain(List<ServerExperience> servers) {
        List<ExperienceGainResult> resultFutures = new ArrayList<>();
        List<CompletableFuture<RoleCalculationResult>> futures = new ArrayList<>();
        CompletableFuture<Void> experienceFuture = new CompletableFuture<>();
        // TODO what if there are a lot in here...., transaction size etc
        servers.forEach(serverExp -> {
            List<CompletableFuture<Member>> memberFutures = new ArrayList<>();
            serverExp.getUserInServerIds().forEach(userInAServerId -> {
                AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(userInAServerId);
                CompletableFuture<Member> memberFuture = memberService.getMemberInServerAsync(userInAServer);
                memberFutures.add(memberFuture);
            });

            FutureUtils.toSingleFutureGeneric(memberFutures).whenComplete((unused, throwable) -> {
                self.updateFoundMembers(memberFutures, serverExp.getServerId(), resultFutures, futures);
                experienceFuture.complete(null);
            });
        });
        return experienceFuture
                .thenCompose(unused -> FutureUtils.toSingleFutureGeneric(futures))
                .whenComplete((unused, throwable) -> self.persistExperienceChanges(resultFutures));
    }

    @Transactional
    public void updateFoundMembers(List<CompletableFuture<Member>> memberFutures, Long serverId, List<ExperienceGainResult> resultFutures, List<CompletableFuture<RoleCalculationResult>> futures) {
        List<AExperienceLevel> levels = experienceLevelManagementService.getLevelConfig();
        SystemConfigProperty defaultExpMultiplier = defaultConfigManagementService.getDefaultConfig(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY);
        SystemConfigProperty defaultMinExp = defaultConfigManagementService.getDefaultConfig(ExperienceFeatureConfig.MIN_EXP_KEY);
        SystemConfigProperty defaultMaxExp = defaultConfigManagementService.getDefaultConfig(ExperienceFeatureConfig.MAX_EXP_KEY);
        AServer server = serverManagementService.loadOrCreate(serverId);
        int minExp = configService.getLongValue(ExperienceFeatureConfig.MIN_EXP_KEY, serverId, defaultMinExp.getLongValue()).intValue();
        int maxExp = configService.getLongValue(ExperienceFeatureConfig.MAX_EXP_KEY, serverId, defaultMaxExp.getLongValue()).intValue();
        Double multiplier = configService.getDoubleValue(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, serverId, defaultExpMultiplier.getDoubleValue());
        PrimitiveIterator.OfInt iterator = new Random().ints(memberFutures.size(), minExp, maxExp + 1).iterator();
        levels.sort(Comparator.comparing(AExperienceLevel::getExperienceNeeded));
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(server);
        List<ADisabledExpRole> disabledExpRoles = disabledExpRoleManagementService.getDisabledRolesForServer(server);
        List<ARole> disabledRoles = disabledExpRoles.stream().map(ADisabledExpRole::getRole).collect(Collectors.toList());
        roles.sort(Comparator.comparing(role -> role.getLevel().getLevel()));
        log.info("Handling {} experiences for server {}. Using {} roles.", memberFutures.size(), serverId, roles.size());
        memberFutures.forEach(future -> {
            if(!future.isCompletedExceptionally()) {
                Integer gainedExperience = iterator.next();
                gainedExperience = (int) Math.floor(gainedExperience * multiplier);
                Member member = future.join();
                AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(member);
                Long userInServerId = userInAServer.getUserInServerId();
                if(!roleService.hasAnyOfTheRoles(member, disabledRoles)) {
                    log.debug("Handling {}. The user might gain {}.", userInServerId, gainedExperience);
                    Optional<AUserExperience> aUserExperienceOptional = userExperienceManagementService.findByUserInServerIdOptional(userInAServer.getUserInServerId());
                    if(aUserExperienceOptional.isPresent()) {
                        AUserExperience aUserExperience = aUserExperienceOptional.get();
                        if(Boolean.FALSE.equals(aUserExperience.getExperienceGainDisabled())) {
                            log.debug("User {} will gain experience.", userInServerId);
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
                            log.debug("Experience gain was disabled. User did not gain any experience.");
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
                    log.debug("User {} has a role which makes the user unable to gain experience.", userInAServer.getUserInServerId());
                }
            }
        });
    }

    /**
     * Calculates the appropriate {@link AExperienceRole experienceRole} based on the current level and awards the referenced the {@link AUserInAServer userinAServer}
     * the {@link net.dv8tion.jda.api.entities.Role role}. If the user already left the guild, this will not award a {@link net.dv8tion.jda.api.entities.Role}, but just
     * return the instance in order to be persisted
     * @param aUserInAServer The {@link AUserInAServer userInAServer} which does not have a {@link AUserExperience userExperience} object,
     *                       therefore we need to calculate the appropriate role and award the role
     * @param roles A list of {@link AExperienceRole experienceRoles} representing the configuration which is used to calculate the appropriate
     *              {@link AExperienceRole experienceRole}
     * @param currentLevel The current level of the user which was reached.
     * @return A {@link CompletableFuture future} containing the {@link RoleCalculationResult result} of the role calculation, which completes after the user has been awarded the role.
     */
    private CompletableFuture<RoleCalculationResult> applyInitialRole(AUserInAServer aUserInAServer, List<AExperienceRole> roles, Integer currentLevel) {
        AExperienceRole role = experienceRoleService.calculateRole(roles, currentLevel);
        if(role == null) {
            log.debug("No experience role calculated. Applying none to user {} in server {}.",
                    aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId());
            return CompletableFuture.completedFuture(RoleCalculationResult
                    .builder()
                    .userInServerId(aUserInAServer.getUserInServerId())
                    .experienceRoleId(null)
                    .build());
        }
        Long experienceRoleId = role.getId();
        Long userInServerId = aUserInAServer.getUserInServerId();
        log.debug("Applying {} as the first experience role for user {} in server {}.",
                experienceRoleId, aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId());
        return roleService.addRoleToUserFuture(aUserInAServer, role.getRole()).thenApply(aVoid -> RoleCalculationResult
                .builder()
                .experienceRoleId(experienceRoleId)
                .userInServerId(userInServerId)
                .build());
    }

    /**
     * Persists the list of {@link ExperienceGainResult results} in the database. If the creation of {@link AUserExperience userExperience} object was requested,
     * this will happen here, also the correct level is selected
     * @param resultFutures A list of {@link ExperienceGainResult results} which define what should be changed for the given {@link AUserExperience userExperience} object:
     *                      The level, experience, experienceRole, message account could change, or the object could not even exist ({@link ExperienceGainResult#createUserExperience})
     */
    @Transactional
    public void persistExperienceChanges(List<ExperienceGainResult> resultFutures) {
        // we do have the _value_ of the level, but we require the actual instance
        List<AExperienceLevel> levels = experienceLevelManagementService.getLevelConfig();
        log.info("Storing {} experience gain results.", resultFutures.size());
        HashMap<Long, List<AExperienceRole>> serverRoleMapping = new HashMap<>();
        resultFutures.forEach(experienceGainResult -> {
            AUserInAServer user = userInServerManagementService.loadOrCreateUser(experienceGainResult.getUserInServerId());
            AUserExperience userExperience;
            if(experienceGainResult.isCreateUserExperience()) {
                userExperience = userExperienceManagementService.createUserInServer(user);
                log.info("Creating new experience user for user in server {}.", experienceGainResult.getUserInServerId());
            } else {
                userExperience = userExperienceManagementService.findByUserInServerId(experienceGainResult.getUserInServerId());
            }
            userExperience.setMessageCount(experienceGainResult.getNewMessageCount());
            userExperience.setExperience(experienceGainResult.getNewExperience());
            // only search the levels if the level changed, or if there is no level currently set
            boolean userExperienceHasLevel = userExperience.getCurrentLevel() != null;
            if(!userExperienceHasLevel || !userExperience.getCurrentLevel().getLevel().equals(experienceGainResult.getNewLevel())) {
                Optional<AExperienceLevel> foundLevel = levels.stream().filter(level -> level.getLevel().equals(experienceGainResult.getNewLevel())).findFirst();
                if(foundLevel.isPresent()) {
                    userExperience.setCurrentLevel(foundLevel.get());
                } else {
                    log.warn("User {} was present, but no level matching the calculation result {} could be found.", userExperience.getUser().getUserReference().getId(), experienceGainResult.getNewLevel());
                }
            }
            AServer server = user.getServerReference();
            // "Caching" the experience roles for this server
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
        log.debug("Updating experience role for user {} in server {}", user.getUserReference().getId(), serverId);
        AExperienceRole role = experienceRoleService.calculateRole(roles, currentLevel);
        boolean currentlyHasNoExperienceRole = userExperience.getCurrentExperienceRole() == null;
        // if calculation results in no role, do not add a role
        if(role == null) {
            log.debug("User {} in server {} does not have an experience role, according to new calculation.",
                    user.getUserReference().getId(), serverId);
            // if the user has a experience role currently, remove it
            if(!currentlyHasNoExperienceRole && !userExperience.getCurrentExperienceRole().getRole().getDeleted()){
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
            if(!userHasRoleAlready || userHasOldRole) {
                CompletableFuture<Void> removalFuture;
                if(userHasOldRole && rolesChanged) {
                    log.info("User {} in server {} loses experience role {}.", userId, serverId, oldUserExperienceRoleId);
                    removalFuture = roleService.removeRoleFromMemberAsync(member, oldUserExperienceRoleId);
                } else {
                    removalFuture = CompletableFuture.completedFuture(null);
                }
                CompletableFuture<Void> addRoleFuture;
                if(!userHasRoleAlready) {
                    log.info("User {} in server {} gets a new role {} because of experience.", userId, serverId, roleId);
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

    @Override
    public List<CompletableFuture<RoleCalculationResult>> syncUserRoles(AServer server) {
        List<CompletableFuture<RoleCalculationResult>> results = new ArrayList<>();
        List<AUserExperience> aUserExperiences = userExperienceManagementService.loadAllUsers(server);
        log.info("Found {} users to synchronize", aUserExperiences.size());
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(server);
        roles.sort(Comparator.comparing(role -> role.getLevel().getLevel()));
        for (int i = 0; i < aUserExperiences.size(); i++) {
            AUserExperience userExperience = aUserExperiences.get(i);
            log.info("Synchronizing {} out of {}. User in Server {}.", i, aUserExperiences.size(), userExperience.getUser().getUserInServerId());
            results.add(updateUserRole(userExperience, roles, userExperience.getCurrentLevel().getLevel()));
        }
        return results;
    }

    @Override
    public CompletableFuture<Void> syncUserRolesWithFeedback(AServer server, Long channelId) {
        AChannel channel = channelManagementService.loadChannel(channelId);
        List<AUserExperience> aUserExperiences = userExperienceManagementService.loadAllUsers(server);
        log.info("Found {} users to synchronize", aUserExperiences.size());
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(server);
        roles.sort(Comparator.comparing(role -> role.getLevel().getLevel()));
        CompletableFutureList<RoleCalculationResult> calculations = executeActionOnUserExperiencesWithFeedBack(aUserExperiences, channel, (AUserExperience experience) -> updateUserRole(experience, roles, experience.getLevelOrDefault()));
        return calculations.getMainFuture().thenAccept(aVoid ->
            self.syncRolesInStorage(calculations.getObjects())
        );
    }

    /**
     * Updates the actually stored experience roles in the database
     * @param results The list of {@link RoleCalculationResult results} which should be applied
     */
    @Transactional
    public void syncRolesInStorage(List<RoleCalculationResult> results) {
        HashMap<Long, AExperienceRole> experienceRoleHashMap = new HashMap<>();
        results.forEach(result -> {
            if(result != null) {
                AUserInAServer user = userInServerManagementService.loadOrCreateUser(result.getUserInServerId());
                AUserExperience userExperience = userExperienceManagementService.findUserInServer(user);
                log.debug("Updating experience role for {} in server {} to {}", user.getUserInServerId(), user.getServerReference().getId(), result.getExperienceRoleId());
                if(result.getExperienceRoleId() != null) {
                    log.debug("User experience {} gets new experience role with id {}.", userExperience.getId(), result.getExperienceRoleId());
                    AExperienceRole role;
                    if(!experienceRoleHashMap.containsKey(result.getExperienceRoleId())) {
                        role = experienceRoleManagementService.getExperienceRoleById(result.getExperienceRoleId());
                        experienceRoleHashMap.put(result.getExperienceRoleId(), role);
                    } else {
                        role = experienceRoleHashMap.get(result.getExperienceRoleId());
                    }
                    userExperience.setCurrentExperienceRole(role);
                } else {
                    log.debug("User experience {} does not get a user experience role.", userExperience.getId());
                    userExperience.setCurrentExperienceRole(null);
                }
            }
        });
    }

    @Override
    public boolean experienceGainEnabledInChannel(MessageChannel messageChannel) {
        AChannel channel = channelManagementService.loadChannel(messageChannel.getIdLong());
        List<AChannelGroup> channelGroups = channelGroupService.getChannelGroupsOfChannelWithType(channel, EXPERIENCE_GAIN_CHANNEL_GROUP_KEY);
        if(!channelGroups.isEmpty()) {
            return channelGroups.stream().noneMatch(AChannelGroup::getEnabled);
        }
        return true;
    }

    @Override
    public AUserExperience createUserExperienceForUser(AUserInAServer aUserInAServer, Long experience, Long messageCount) {
        List<AExperienceLevel> levels = experienceLevelManagementService.getLevelConfig();
        levels.sort(Comparator.comparing(AExperienceLevel::getExperienceNeeded));
        return createUserExperienceForUser(aUserInAServer, experience, messageCount, levels);
    }

    @Override
    public AUserExperience createUserExperienceForUser(AUserInAServer aUserInAServer, Long experience, Long messageCount, List<AExperienceLevel> levels) {
        AExperienceLevel level = calculateLevel(levels, experience);
        AUserExperience userExperience = userExperienceManagementService.createUserInServer(aUserInAServer);
        userExperience.setCurrentLevel(level);
        userExperience.setExperience(experience);
        userExperience.setMessageCount(messageCount);
        return userExperience;
    }

    @Override
    public CompletableFutureList<RoleCalculationResult> executeActionOnUserExperiencesWithFeedBack(List<AUserExperience> experiences, AChannel channel, Function<AUserExperience, CompletableFuture<RoleCalculationResult>> toExecute) {
        List<CompletableFuture<RoleCalculationResult>> futures = new ArrayList<>();
        Long serverId = channel.getServer().getId();
        MessageToSend status = getUserSyncStatusUpdateModel(0, experiences.size(), serverId);
        Message statusMessage = messageService.createStatusMessage(status, channel).join();
        int interval = Math.min(Math.max(experiences.size() / 10, 1), 100);
        for (int i = 0; i < experiences.size(); i++) {
            if((i % interval) == 1) {
                log.info("Updating feedback message with new index {} out of {}.", i, experiences.size());
                status = getUserSyncStatusUpdateModel(i, experiences.size(), serverId);
                messageService.updateStatusMessage(channel, statusMessage.getIdLong(), status);
            }
            AUserExperience userExperience = experiences.get(i);
            futures.add(toExecute.apply(userExperience));
            log.debug("Synchronizing {} out of {}. User in server ID {}.", i, experiences.size(), userExperience.getUser().getUserInServerId());
        }
        status = getUserSyncStatusUpdateModel(experiences.size(), experiences.size(), serverId);
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
        log.info("Enabling experience gain for user {} in server {}.", userInAServer.getUserReference().getId(), userInAServer.getServerReference().getId());
        AUserExperience userExperience = userExperienceManagementService.findUserInServer(userInAServer);
        userExperience.setExperienceGainDisabled(false);
    }

    /**
     * Renders a {@link MessageToSend messageToSend} to be used as a status message for the ongoing user synchronization
     * @param current The amount of users which have been synced
     * @param total The total amount of users which will be synced
     * @param serverId The ÍD of the {@link AServer server} for which this synchronization is being executed
     * @return A {@link MessageToSend messageToSend} which will be used to inform the user executing the synchronization about the new situation
     */
    private MessageToSend getUserSyncStatusUpdateModel(Integer current, Integer total, Long serverId) {
        UserSyncStatusModel statusModel = UserSyncStatusModel.builder().currentCount(current).totalUserCount(total).build();
        return templateService.renderEmbedTemplate("user_sync_status_message", statusModel, serverId);
    }

    @Override
    public CompletableFuture<RoleCalculationResult> syncForSingleUser(AUserExperience userExperience) {
        AUserInAServer user = userExperience.getUser();
        log.info("Synchronizing for user {} in server {}.", user.getUserReference().getId(), user.getServerReference().getId());
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(user.getServerReference());
        roles.sort(Comparator.comparing(role -> role.getLevel().getLevel()));
        return updateUserRole(userExperience, roles, userExperience.getLevelOrDefault());
    }

    @Override
    public LeaderBoard findLeaderBoardData(AServer server, Integer page) {
        if(page <= 0) {
            throw new IllegalArgumentException("Page needs to be >= 1");
        }
        page--;
        int pageSize = 10;
        log.debug("Loading leaderboard page {} for server {}.", page, server.getId());
        List<AUserExperience> experiences = userExperienceManagementService.findLeaderBoardUsersPaginated(server, page * pageSize, (page + 1) * pageSize);
        List<LeaderBoardEntry> entries = new ArrayList<>();
        log.debug("Found {} experiences.", experiences.size());
        for (int i = 0; i < experiences.size(); i++) {
            AUserExperience userExperience = experiences.get(i);
            entries.add(LeaderBoardEntry.builder().experience(userExperience).rank((page * pageSize) + i + 1).build());
        }
        return LeaderBoard.builder().entries(entries).build();
    }

    @Override
    public LeaderBoardEntry getRankOfUserInServer(AUserInAServer userInAServer) {
        log.debug("Retrieving rank for {}", userInAServer.getUserReference().getId());
        Optional<AUserExperience> aUserExperienceOptional = userExperienceManagementService.findByUserInServerIdOptional(userInAServer.getUserInServerId());
        if(!aUserExperienceOptional.isPresent()) {
            throw new NoExperienceTrackedException();
        }
        Integer rank = 0;
        AUserExperience aUserExperience = aUserExperienceOptional.get();
        LeaderBoardEntryResult rankOfUserInServer = userExperienceManagementService.getRankOfUserInServer(aUserExperience);
        if(rankOfUserInServer != null) {
            rank = rankOfUserInServer.getRank();
        }
        return LeaderBoardEntry.builder().experience(aUserExperience).rank(rank).build();
    }

}

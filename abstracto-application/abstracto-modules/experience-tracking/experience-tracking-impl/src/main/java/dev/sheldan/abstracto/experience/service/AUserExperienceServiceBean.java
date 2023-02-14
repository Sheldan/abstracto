package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureConfig;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureMode;
import dev.sheldan.abstracto.experience.exception.NoExperienceTrackedException;
import dev.sheldan.abstracto.experience.model.LeaderBoard;
import dev.sheldan.abstracto.experience.model.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.model.RoleCalculationResult;
import dev.sheldan.abstracto.experience.model.database.*;
import dev.sheldan.abstracto.experience.model.template.LevelUpNotificationModel;
import dev.sheldan.abstracto.experience.model.template.UserSyncStatusModel;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static dev.sheldan.abstracto.experience.config.ExperienceFeatureConfig.EXP_COOLDOWN_SECONDS_KEY;

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
    private SecureRandom secureRandom;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private AUserExperienceServiceBean self;

    @Autowired
    @Qualifier("experienceUpdateExecutor")
    private TaskExecutor experienceUpdateExecutor;

    @Override
    public void addExperience(Member member, Message message) {
        runTimeExperienceService.takeLock();
        try {
            Map<Long, Map<Long, Instant>> runtimeExperience = runTimeExperienceService.getRuntimeExperience();
            Long serverId = member.getGuild().getIdLong();
            Long userId = member.getIdLong();
            boolean receivesNewExperience = false;
            if(!runtimeExperience.containsKey(serverId)) {
                runtimeExperience.put(serverId, new HashMap<>());
                receivesNewExperience = true;
            } else {
                Map<Long, Instant> serverExperience = runtimeExperience.get(serverId);
                if(!serverExperience.containsKey(userId)) {
                    receivesNewExperience = true;
                } else {
                    Instant latestExperience = serverExperience.get(userId);
                    if(latestExperience.isBefore(Instant.now())) {
                        receivesNewExperience = true;
                    }
                }
            }
            if(receivesNewExperience) {
                Map<Long, Instant> serverExperience = runtimeExperience.get(serverId);
                // we store when the user is eligible for experience _again_
                Long maxSeconds = configService.getLongValueOrConfigDefault(EXP_COOLDOWN_SECONDS_KEY, serverId);
                serverExperience.put(userId, Instant.now().plus(maxSeconds, ChronoUnit.SECONDS));
                CompletableFuture.runAsync(() -> self.addExperienceToMember(member, message), experienceUpdateExecutor).exceptionally(throwable -> {
                    log.error("Failed to add experience to member {} in server {}.", message.getAuthor().getId(), message.getGuild().getIdLong(), throwable);
                    return null;
                });
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

    @Override
    public CompletableFuture<Void> syncUserRolesWithFeedback(AServer server, MessageChannel messageChannel) {
        List<AUserExperience> aUserExperiences = userExperienceManagementService.loadAllUsers(server);
        List<Long> userIds = aUserExperiences
                .stream()
                .map(aUserExperience -> aUserExperience.getUser().getUserReference().getId())
                .collect(Collectors.toList());
        log.info("Synchronizing experience roles for {} users.", userIds.size());
        CompletableFuture<Void> returnFuture = new CompletableFuture<>();
        Long serverId = server.getId();
        int supposedUserCount = userIds.size();

        List<List<Long>> partitionedUserIds = ListUtils.partition(userIds, 100);
        List<CompletableFuture<List<Member>>> memberLoadingFutures = new ArrayList<>();
        for (List<Long> userIdsPart : partitionedUserIds) {
            memberLoadingFutures.add(memberService.getMembersInServerAsync(server.getId(), userIdsPart));
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                log.error("Failed to sleep.", e);
            }
        }
        CompletableFutureList<List<Member>> listCompletableFutureList = new CompletableFutureList<>(memberLoadingFutures);
        listCompletableFutureList.getMainFuture().whenComplete((result, throwable) -> {
                List<Member> members = new ArrayList<>();
                listCompletableFutureList.getFutures().forEach(listCompletableFuture -> members.addAll(listCompletableFuture.join()));
                if(throwable != null) {
                    log.warn("Failed to load all members in server {} for syncing experience. We started with {} and got {}.",
                            serverId, supposedUserCount, members.size(), throwable);
                }
                self.syncUsers(members, serverId, messageChannel).thenAccept(unused -> {
                    log.info("Finished syncing users for experience roles.");
                    returnFuture.complete(null);
                }).exceptionally(throwable1 -> {
                    log.error("Failed to sync members.", throwable);
                    returnFuture.complete(null);
                    return null;
                });
            }).exceptionally(throwable -> {
                log.error("Failed to load members.", throwable);
                returnFuture.complete(null);
                return null;
        });
        return returnFuture;
    }

    @Transactional
    public CompletableFuture<Void> syncUsers(List<Member> members, Long serverId, MessageChannel messageChannel) {
        AtomicInteger currentCount = new AtomicInteger();
        MessageToSend status = getUserSyncStatusUpdateModel(0, members.size(), serverId);
        Message statusMessage = messageService.createStatusMessage(status, messageChannel).join();

        AServer server = serverManagementService.loadServer(serverId);
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(server);
        roles.sort(Comparator.comparing(role -> role.getLevel().getLevel()));

        List<CompletableFuture<Void>> futures = members
                .stream()
                .map(member -> this.syncUser(member, roles)
                        .thenAccept(unused -> {
                            currentCount.set(currentCount.get() + 1);
                            log.debug("Finished synchronizing {} users.", currentCount.get());
                            if(currentCount.get() % 50 == 0) {
                                log.info("Notifying for {} current users with synchronize.", currentCount.get());
                                MessageToSend newStatus = getUserSyncStatusUpdateModel(currentCount.get(), members.size(), serverId);
                                messageService.updateStatusMessage(messageChannel, statusMessage.getIdLong(), newStatus);
                            }
                        }))
                .collect(Collectors.toList());
        return FutureUtils.toSingleFutureGeneric(futures).thenAccept(unused -> {
            MessageToSend newStatus = getUserSyncStatusUpdateModel(currentCount.get(), members.size(), serverId);
            messageService.updateStatusMessage(messageChannel, statusMessage.getIdLong(), newStatus);
        });
    }

    public CompletableFuture<Void> syncUser(Member member, List<AExperienceRole> roles) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
        AUserExperience userExperience = userExperienceManagementService.findByUserInServerId(aUserInAServer.getUserInServerId());
        return  calculateAndApplyExperienceRole(userExperience, member, roles, false);
    }

    @Override
    public CompletableFuture<Void> syncForSingleUser(AUserExperience userExperience, Member member, boolean forceRoles) {
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(userExperience.getServer());
        roles.sort(Comparator.comparing(role -> role.getLevel().getLevel()));
        return calculateAndApplyExperienceRole(userExperience, member, roles, forceRoles);
    }

    private CompletableFuture<Void> calculateAndApplyExperienceRole(AUserExperience userExperience, Member member, List<AExperienceRole> roles, boolean forceRoles) {
        AExperienceRole calculatedNewRole = experienceRoleService.calculateRole(roles, userExperience.getCurrentLevel().getLevel());
        Long oldRoleId = userExperience.getCurrentExperienceRole() != null && userExperience.getCurrentExperienceRole().getRole() != null ? userExperience.getCurrentExperienceRole().getRole().getId() : null;
        Long newRoleId = calculatedNewRole != null ? calculatedNewRole.getRole().getId() : null;

        userExperience.setCurrentExperienceRole(calculatedNewRole);

        CompletableFuture<Void> returningFuture;
        if(!Objects.equals(oldRoleId, newRoleId) || forceRoles) {
            CompletableFuture<Void> addingFuture;
            if(oldRoleId != null || forceRoles) {
                addingFuture = roleService.removeRoleFromMemberAsync(member, oldRoleId);
            } else {
                addingFuture = CompletableFuture.completedFuture(null);
            }
            CompletableFuture<Void> removingFeature;
            if(newRoleId != null || forceRoles) {
                removingFeature = roleService.addRoleToMemberAsync(member, newRoleId);
            } else {
                removingFeature = CompletableFuture.completedFuture(null);
            }
            returningFuture = CompletableFuture.allOf(addingFuture, removingFeature);
        } else {
            returningFuture = CompletableFuture.completedFuture(null);
        }
        return returningFuture;
    }

    @Transactional
    public void addExperienceToMember(Member member, Message message) {
        long serverId = member.getGuild().getIdLong();
        AServer server = serverManagementService.loadOrCreate(serverId);
        List<ADisabledExpRole> disabledExpRoles = disabledExpRoleManagementService.getDisabledRolesForServer(server);
        List<ARole> disabledRoles = disabledExpRoles
                .stream()
                .map(ADisabledExpRole::getRole)
                .collect(Collectors.toList());
        if(roleService.hasAnyOfTheRoles(member, disabledRoles)) {
            log.debug("User {} has a experience disable role in server {} - not giving any experience.", member.getIdLong(), serverId);
            return;
        }
        List<AExperienceLevel> levels = experienceLevelManagementService.getLevelConfig();
        levels.sort(Comparator.comparing(AExperienceLevel::getExperienceNeeded));

        Long minExp = configService.getLongValueOrConfigDefault(ExperienceFeatureConfig.MIN_EXP_KEY, serverId);
        Long maxExp = configService.getLongValueOrConfigDefault(ExperienceFeatureConfig.MAX_EXP_KEY, serverId);
        Double multiplier = configService.getDoubleValueOrConfigDefault(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, serverId);
        Long experienceRange = maxExp - minExp + 1;
        Long gainedExperience = (secureRandom.nextInt(experienceRange.intValue()) + minExp);
        gainedExperience = (long) Math.floor(gainedExperience * multiplier);

        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(server);
        roles.sort(Comparator.comparing(role -> role.getLevel().getLevel()));

        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(member);
        Long userInServerId = userInAServer.getUserInServerId();
        log.debug("Handling {}. The user might gain {}.", userInServerId, gainedExperience);
        Optional<AUserExperience> aUserExperienceOptional = userExperienceManagementService.findByUserInServerIdOptional(userInAServer.getUserInServerId());
        AUserExperience aUserExperience = aUserExperienceOptional.orElseGet(() -> userExperienceManagementService.createUserInServer(userInAServer));
        if(Boolean.FALSE.equals(aUserExperience.getExperienceGainDisabled())) {
            Long oldExperience = aUserExperience.getExperience();
            Long newExperienceCount = oldExperience + gainedExperience;
            aUserExperience.setExperience(newExperienceCount);
            AExperienceLevel newLevel = calculateLevel(levels, newExperienceCount);
            RoleCalculationResult result = RoleCalculationResult
                    .builder()
                    .build();
            if(!Objects.equals(newLevel.getLevel(), aUserExperience.getCurrentLevel().getLevel())) {
                Integer oldLevel = aUserExperience.getCurrentLevel() != null ? aUserExperience.getCurrentLevel().getLevel() : 0;
                log.info("User {} in server {} changed level. New {}, Old {}.", member.getIdLong(),
                        member.getGuild().getIdLong(), newLevel.getLevel(),
                        oldLevel);
                aUserExperience.setCurrentLevel(newLevel);
                AExperienceRole calculatedNewRole = experienceRoleService.calculateRole(roles, newLevel.getLevel());
                Long oldRoleId = aUserExperience.getCurrentExperienceRole() != null && aUserExperience.getCurrentExperienceRole().getRole() != null ? aUserExperience.getCurrentExperienceRole().getRole().getId() : null;
                Long newRoleId = calculatedNewRole != null && calculatedNewRole.getRole() != null ? calculatedNewRole.getRole().getId() : null;
                result.setOldRoleId(oldRoleId);
                result.setNewRoleId(newRoleId);
                if(message != null
                        && aUserExperience.getLevelUpNotification()
                        && featureModeService.featureModeActive(ExperienceFeatureDefinition.EXPERIENCE, serverId, ExperienceFeatureMode.LEVEL_UP_NOTIFICATION)) {
                    LevelUpNotificationModel model = LevelUpNotificationModel
                            .builder()
                            .memberDisplay(MemberDisplay.fromMember(member))
                            .oldExperience(oldExperience)
                            .newExperience(newExperienceCount)
                            .newLevel(newLevel.getLevel())
                            .oldLevel(oldLevel)
                            .newRole(oldRoleId != null ? RoleDisplay.fromRole(oldRoleId) : null)
                            .newRole(newRoleId != null ? RoleDisplay.fromRole(newRoleId) : null)
                            .build();
                    MessageToSend messageToSend = templateService.renderEmbedTemplate("experience_level_up_notification", model);
                    FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, message.getChannel())).thenAccept(unused -> {
                        log.info("Sent level up notification to user {} in server {} in channel {}.", member.getIdLong(), serverId, message.getChannel().getIdLong());
                    }).exceptionally(throwable -> {
                        log.warn("Failed to send level up notification to user {} in server {} in channel {}.", member.getIdLong(), serverId, message.getChannel().getIdLong());
                        return null;
                    });
                }
                aUserExperience.setCurrentExperienceRole(calculatedNewRole);
            }
            aUserExperience.setMessageCount(aUserExperience.getMessageCount() + 1L);
            if(!aUserExperienceOptional.isPresent()) {
                userExperienceManagementService.saveUser(aUserExperience);
            }
            if(!Objects.equals(result.getOldRoleId(), result.getNewRoleId())) {
                if(result.getOldRoleId() != null) {
                    roleService.removeRoleFromMemberAsync(member, result.getOldRoleId()).thenAccept(unused -> {
                        log.debug("Removed role {} to member {} in server {}.", result.getOldRoleId(), member.getIdLong(), member.getGuild().getIdLong());
                    }).exceptionally(throwable -> {
                        log.warn("Failed to remove role {} from {} member {} in server {}.", result.getOldRoleId(), member.getIdLong(), member.getGuild().getIdLong(), throwable);
                        return null;
                    });
                }
                if(result.getNewRoleId() != null) {
                    roleService.addRoleToMemberAsync(member, result.getNewRoleId()).thenAccept(unused -> {
                        log.debug("Added role {} to member {} in server {}.", result.getOldRoleId(), member.getIdLong(), member.getGuild().getIdLong());
                    }).exceptionally(throwable -> {
                        log.warn("Failed to add role {} to {} member {} in server {}.", result.getOldRoleId(), member.getIdLong(), member.getGuild().getIdLong(), throwable);
                        return null;
                    });
                }
            }
        } else {
            log.debug("Experience gain was disabled. User did not gain any experience.");
        }
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

    @Override
    public void setLevelUpNotification(AUserInAServer aUserInAServer, Boolean newValue) {
        Optional<AUserExperience> aUserExperienceOptional = userExperienceManagementService.findByUserInServerIdOptional(aUserInAServer.getUserInServerId());
        AUserExperience aUserExperience = aUserExperienceOptional.orElseGet(() -> userExperienceManagementService.createUserInServer(aUserInAServer));
        aUserExperience.setLevelUpNotification(newValue);
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
    public LeaderBoard findLeaderBoardData(AServer server, Integer page) {
        if(page <= 0) {
            throw new IllegalArgumentException("Page needs to be >= 1");
        }
        page--;
        int pageSize = 10;
        log.debug("Loading leaderboard page {} for server {}.", page, server.getId());
        List<AUserExperience> experiences = userExperienceManagementService.findLeaderBoardUsersPaginated(server, page, pageSize);
        List<LeaderBoardEntry> entries = new ArrayList<>();
        log.debug("Found {} experiences.", experiences.size());
        int pageOffset = page * pageSize;
        for (int i = 0; i < experiences.size(); i++) {
            AUserExperience userExperience = experiences.get(i);
            entries.add(LeaderBoardEntry.builder().experience(userExperience).rank(pageOffset + i + 1).build());
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

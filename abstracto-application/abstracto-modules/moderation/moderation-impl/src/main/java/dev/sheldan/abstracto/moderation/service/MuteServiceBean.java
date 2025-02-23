package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.feature.MutingFeatureConfig;
import dev.sheldan.abstracto.moderation.config.posttarget.MutingPostTarget;
import dev.sheldan.abstracto.moderation.exception.NoMuteFoundException;
import dev.sheldan.abstracto.moderation.model.MuteResult;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.database.Mute;
import dev.sheldan.abstracto.moderation.model.template.command.MuteLogModel;
import dev.sheldan.abstracto.moderation.model.template.command.MuteNotification;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MuteServiceBean implements MuteService {

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private MuteManagementService muteManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private GuildService guildService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private MuteServiceBean self;

    @Autowired
    @Qualifier("unmuteScheduler")
    private ScheduledExecutorService unMuteScheduler;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private CounterService counterService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private InfractionService infractionService;

    public static final String MUTE_LOG_TEMPLATE = "mute_log";
    public static final String MUTE_NOTIFICATION_TEMPLATE = "mute_notification";
    public static final String MUTE_COUNTER_KEY = "MUTES";

    @Override
    public CompletableFuture<MuteResult> muteUserInServer(Guild guild, ServerUser userBeingMuted, String reason, Duration duration) {
        Long serverId = guild.getIdLong();
        Instant targetDate = Instant.now().plus(duration);
        MuteNotification muteNotificationModel = MuteNotification
                .builder()
                .muteTargetDate(targetDate)
                .reason(reason)
                .serverName(guild.getName())
                .build();
        MuteResult[] result = {MuteResult.SUCCESSFUL};
        log.info("Notifying the user about the mute.");
        String muteNotificationMessage = templateService.renderTemplate(MUTE_NOTIFICATION_TEMPLATE, muteNotificationModel, serverId);
        return messageService.sendMessageToUser(userBeingMuted, muteNotificationMessage)
                .exceptionally(throwable -> {
                    log.warn("Failed to notify about mute", throwable);
                    result[0] = MuteResult.NOTIFICATION_FAILED;
                    return null;
                })
                .thenCompose(unused -> memberService.timeoutMember(guild, userBeingMuted, duration, reason))
                .thenApply(message -> result[0]);
    }

    private void createMuteObject(ServerUser userToMute, ServerUser mutingUser, String reason, Instant targetDate, Long muteId,
                                  String triggerKey, Long infractionId, ServerChannelMessage serverChannelMessage) {
        AChannel channel = channelManagementService.loadChannel(serverChannelMessage.getChannelId());
        AServerAChannelMessage origin = AServerAChannelMessage
                .builder()
                .channel(channel)
                .server(channel.getServer())
                .build();
        AUserInAServer userInServerBeingMuted = userInServerManagementService.loadOrCreateUser(userToMute);
        AUserInAServer userInServerMuting = userInServerManagementService.loadOrCreateUser(mutingUser);
        muteManagementService.createMute(userInServerBeingMuted, userInServerMuting, reason, targetDate, origin, triggerKey, muteId, infractionId);
    }

    @Override
    public String startUnMuteJobFor(Instant unMuteDate, Long muteId, Long serverId) {
        Duration muteDuration = Duration.between(Instant.now(), unMuteDate);
        if(muteDuration.getSeconds() < 60) {
            log.debug("Directly scheduling the unMute, because it was below the threshold.");
            unMuteScheduler.schedule(() -> {
                try {
                    self.endMute(muteId, serverId);
                } catch (Exception exception) {
                    log.error("Failed to remind immediately.", exception);
                }
            }, muteDuration.toNanos(), TimeUnit.NANOSECONDS);
            return null;
        } else {
            log.debug("Starting scheduled job to execute unMute.");
            HashMap<Object, Object> parameters = new HashMap<>();
            parameters.put("muteId", muteId.toString());
            parameters.put("serverId", serverId.toString());
            JobParameters jobParameters = JobParameters.builder().parameters(parameters).build();
            return schedulerService.executeJobWithParametersOnce("unMuteJob", "moderation", jobParameters, Date.from(unMuteDate));
        }
    }

    @Override
    public void cancelUnMuteJob(Mute mute) {
        if(mute.getTriggerKey() != null) {
            log.info("Cancelling un-mute job for mute {} in server {}.", mute.getMuteId().getId(), mute.getServer().getId());
            schedulerService.stopTrigger(mute.getTriggerKey());
        }
    }

    @Override
    @Transactional
    public CompletableFuture<MuteResult> muteMemberWithLog(ServerUser userToMute, ServerUser mutingUser, String reason, Duration duration, Guild guild, ServerChannelMessage origin) {
        return muteMemberWithLog(userToMute, mutingUser, reason, duration, guild, origin, null);
    }

    @Override
    public CompletableFuture<MuteResult> muteMemberWithLog(ServerUser userToMute, ServerUser mutingUser, String reason, Duration duration, Guild guild, ServerChannelMessage origin, Instant oldTimeout) {
        Long serverId = userToMute.getServerId();
        Instant targetDate = Instant.now().plus(duration);
        log.info("Muting member {} in server {}.", userToMute.getUserId(), serverId);
        AServer server = serverManagementService.loadOrCreate(serverId);
        Long muteId = counterService.getNextCounterValue(server, MUTE_COUNTER_KEY);
        CompletableFuture<MuteResult> result = muteUserInServer(guild, userToMute, reason, duration);
        return result
                .thenCompose(muteResult -> self.composeAndLogMute(userToMute, mutingUser, reason, duration, guild, oldTimeout))
                .thenCompose(logMessage -> self.evaluateAndStoreInfraction(userToMute, mutingUser, reason, targetDate))
                .thenAccept(infractionId -> self.persistMute(userToMute, mutingUser, targetDate, muteId, reason, infractionId, origin))
                .thenApply(unused -> result.join());
    }

    @Transactional
    public CompletableFuture<Void> composeAndLogMute(ServerUser userToMute, ServerUser mutingUser, String reason, Duration duration, Guild guild, Instant oldTimeout) {
        CompletableFuture<Member> mutedMemberFuture = memberService.retrieveMemberInServer(userToMute);
        CompletableFuture<Member> mutingMemberFuture = memberService.retrieveMemberInServer(mutingUser);
        Instant targetDate = Instant.now().plus(duration);
        return CompletableFuture.allOf(mutedMemberFuture, mutingMemberFuture).thenCompose(unused -> {
            Member mutedMember = mutedMemberFuture.join();
            Member mutingMember = mutingMemberFuture.join();
            MuteLogModel muteLogModel = MuteLogModel
                    .builder()
                    .muteTargetDate(targetDate)
                    .oldMuteTargetDate(oldTimeout)
                    .mutingMember(MemberDisplay.fromMember(mutingMember))
                    .mutedMember(MemberDisplay.fromMember(mutedMember))
                    .duration(duration)
                    .reason(reason)
                    .build();
            return self.sendMuteLogMessage(muteLogModel, guild.getIdLong());
        }).exceptionally(throwable -> {
            log.warn("Failed to load users for mute log ({}, {}) in guild {}.", userToMute.getUserId(), mutingUser.getUserId(), guild.getIdLong(), throwable);
            MuteLogModel muteLogModel = MuteLogModel
                    .builder()
                    .muteTargetDate(targetDate)
                    .oldMuteTargetDate(null)
                    .mutingMember(MemberDisplay.fromServerUser(mutingUser))
                    .mutedMember(MemberDisplay.fromServerUser(userToMute))
                    .duration(duration)
                    .reason(reason)
                    .build();
            self.sendMuteLogMessage(muteLogModel, guild.getIdLong());
            return null;
        });
    }

    @Transactional
    public CompletableFuture<Long> evaluateAndStoreInfraction(ServerUser userToMute, ServerUser mutingUser, String reason, Instant targetDate) {
        Long serverId = userToMute.getServerId();
        if(featureFlagService.getFeatureFlagValue(ModerationFeatureDefinition.INFRACTIONS, serverId)) {
            Long infractionPoints = configService.getLongValueOrConfigDefault(MutingFeatureConfig.MUTE_INFRACTION_POINTS, serverId);
            AUserInAServer mutedUserInAServer = userInServerManagementService.loadOrCreateUser(userToMute);
            AUserInAServer mutingUserInAServer = userInServerManagementService.loadOrCreateUser(mutingUser);
            Map<String, String> parameters = new HashMap<>();
            parameters.put(INFRACTION_PARAMETER_DURATION_KEY, templateService.renderDuration(Duration.between(Instant.now(), targetDate), serverId));
            return infractionService.createInfractionWithNotification(mutedUserInAServer, infractionPoints, MUTE_INFRACTION_TYPE, reason, mutingUserInAServer, parameters)
                    .thenApply(Infraction::getId);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Transactional
    public void persistMute(ServerUser userToMute, ServerUser mutingUser, Instant targetDate, Long muteId, String reason, Long infractionId, ServerChannelMessage origin) {
        completelyUnMuteMember(userToMute);
        String triggerKey = startUnMuteJobFor(targetDate, muteId, userToMute.getServerId());
        createMuteObject(userToMute, mutingUser, reason, targetDate, muteId, triggerKey, infractionId, origin);
    }

    @Override
    @Transactional
    public CompletableFuture<Void> unMuteUser(ServerUser userToUnmute, ServerUser unMutingUser, Guild guild) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(userToUnmute);
        boolean muteActive = muteManagementService.hasActiveMute(aUserInAServer);
        if(!muteActive) {
            return memberService.removeTimeout(guild, userToUnmute, null);
        } else {
            Mute mute = muteManagementService.getAMuteOf(aUserInAServer);
            return endMute(mute, guild);
        }
    }

    @Override
    public CompletableFuture<Void> endMute(Mute mute, Guild guild) {
        if(mute.getMuteEnded()) {
            log.info("Mute {} in server {} has already ended. Not unmuting.", mute.getMuteId().getId(), mute.getMuteId().getServerId());
            return CompletableFuture.completedFuture(null);
        }
        Long muteId = mute.getMuteId().getId();
        AServer mutingServer = mute.getServer();
        ServerUser mutedUser = ServerUser.fromAUserInAServer(mute.getMutedUser());
        ServerUser mutingUser = ServerUser.fromAUserInAServer(mute.getMutingUser());
        log.info("UnMuting {} in server {}", mute.getMutedUser().getUserReference().getId(), mutingServer.getId());
        return memberService.removeTimeout(guild, mutedUser, null)
                .thenCompose(unused -> self.composeAndLogUnmute(mutedUser, mutingUser, guild))
                .thenAccept(unused -> {
                    if(muteId != null) {
                        self.endMuteInDatabase(muteId, guild.getIdLong());
                    }
                });
    }

    @Transactional
    public CompletableFuture<Void> composeAndLogUnmute(ServerUser mutedUser, ServerUser mutingUser, Guild guild) {
        CompletableFuture<Member> mutedMemberFuture = memberService.retrieveMemberInServer(mutedUser);
        CompletableFuture<Member> mutingMemberFuture = memberService.retrieveMemberInServer(mutingUser);
        return CompletableFuture.allOf(mutedMemberFuture, mutingMemberFuture).thenCompose(unused -> {
            Member mutedMember = mutedMemberFuture.join();
            Member mutingMember = mutingMemberFuture.join();
            MuteLogModel muteLogModel = MuteLogModel
                    .builder()
                    .muteTargetDate(null)
                    .oldMuteTargetDate(null)
                    .mutingMember(MemberDisplay.fromMember(mutingMember))
                    .mutedMember(MemberDisplay.fromMember(mutedMember))
                    .duration(null)
                    .reason(null)
                    .build();
            return self.sendMuteLogMessage(muteLogModel, guild.getIdLong());
        });
    }

    @Transactional
    public void endMuteInDatabase(Long muteId, Long serverId) {
        Optional<Mute> muteOptional = muteManagementService.findMuteOptional(muteId, serverId);
        muteOptional.ifPresent(mute ->
            completelyUnMuteUser(mute.getMutedUser())
        );
    }

    @Override
    @Transactional
    public CompletableFuture<Void> endMute(Long muteId, Long serverId) {
        log.info("UnMuting the mute {} in server {}", muteId, serverId);
        Optional<Mute> muteOptional = muteManagementService.findMuteOptional(muteId, serverId);
        if(muteOptional.isPresent()) {
            Guild guild = guildService.getGuildById(serverId);
            return endMute(muteOptional.get(), guild);
        } else {
            throw new NoMuteFoundException();
        }
    }

    @Override
    public CompletableFuture<Void> sendMuteLogMessage(MuteLogModel model, Long serverId) {
        MessageToSend message = templateService.renderEmbedTemplate(MuteServiceBean.MUTE_LOG_TEMPLATE, model, serverId);
        return FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(message, MutingPostTarget.MUTE_LOG, serverId));
    }

    @Override
    public void completelyUnMuteUser(AUserInAServer aUserInAServer) {
        log.info("Completely unmuting user {} in server {}.", aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId());
        List<Mute> allMutesOfUser = muteManagementService.getAllActiveMutesOf(aUserInAServer);
        allMutesOfUser.forEach(mute -> {
            mute.setMuteEnded(true);
            cancelUnMuteJob(mute);
            muteManagementService.saveMute(mute);
        });
    }

    @Override
    public void completelyUnMuteMember(ServerUser serverUser) {
        completelyUnMuteUser(userInServerManagementService.loadOrCreateUser(serverUser));
    }

}

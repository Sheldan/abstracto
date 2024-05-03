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
import dev.sheldan.abstracto.moderation.model.template.command.MuteListenerModel;
import dev.sheldan.abstracto.moderation.model.template.command.MuteNotification;
import dev.sheldan.abstracto.moderation.model.template.command.UnMuteLog;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
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
    public CompletableFuture<MuteResult> muteMemberWithLog(ServerUser userToMute, ServerUser mutingUser, String reason, Duration duration, Guild guild, ServerChannelMessage origin) {
        Long serverId = userToMute.getServerId();
        Instant targetDate = Instant.now().plus(duration);
        log.debug("Muting member {} in server {}.", userToMute.getUserId(), serverId);
        AServer server = serverManagementService.loadOrCreate(serverId);
        Long muteId = counterService.getNextCounterValue(server, MUTE_COUNTER_KEY);
        CompletableFuture<MuteResult> result = muteUserInServer(guild, userToMute, reason, duration);
        return result
                .thenCompose(unused -> self.sendMuteLog(userToMute, mutingUser, duration, reason))
                .thenCompose(logMessage -> self.evaluateAndStoreInfraction(userToMute, mutingUser, reason, targetDate, logMessage))
                .thenAccept(infractionId -> self.persistMute(userToMute, mutingUser, targetDate, muteId, reason, infractionId, origin))
                .thenApply(unused -> result.join());
    }

    @Transactional
    public CompletableFuture<Long> evaluateAndStoreInfraction(ServerUser userToMute, ServerUser mutingUser, String reason, Instant targetDate, Message logMessage) {
        Long serverId = userToMute.getServerId();
        if(featureFlagService.getFeatureFlagValue(ModerationFeatureDefinition.INFRACTIONS, serverId)) {
            Long infractionPoints = configService.getLongValueOrConfigDefault(MutingFeatureConfig.MUTE_INFRACTION_POINTS, serverId);
            AUserInAServer mutedUserInAServer = userInServerManagementService.loadOrCreateUser(userToMute);
            AUserInAServer mutingUserInAServer = userInServerManagementService.loadOrCreateUser(mutingUser);
            Map<String, String> parameters = new HashMap<>();
            parameters.put(INFRACTION_PARAMETER_DURATION_KEY, templateService.renderDuration(Duration.between(Instant.now(), targetDate), serverId));
            return infractionService.createInfractionWithNotification(mutedUserInAServer, infractionPoints, MUTE_INFRACTION_TYPE, reason, mutingUserInAServer, parameters, logMessage)
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

    @Transactional
    public CompletableFuture<Message> sendMuteLog(ServerUser userBeingMuted, ServerUser mutingUser, Duration duration, String reason)  {
        Instant targetDate = Instant.now().plus(duration);
        MuteListenerModel model = MuteListenerModel
                .builder()
                .mutedUser(MemberDisplay.fromServerUser(userBeingMuted))
                .mutingUser(MemberDisplay.fromServerUser(mutingUser))
                .oldMuteTargetDate(null)
                .duration(duration)
                .muteTargetDate(targetDate)
                .reason(reason)
                .build();
        log.debug("Sending mute log to the mute post target.");
        Long serverId = userBeingMuted.getServerId();
        MessageToSend message = templateService.renderEmbedTemplate(MUTE_LOG_TEMPLATE, model, serverId);
        List<CompletableFuture<Message>> futures = postTargetService.sendEmbedInPostTarget(message, MutingPostTarget.MUTE_LOG, serverId);
        return FutureUtils.toSingleFutureGeneric(futures).thenApply(unused -> futures.get(0).join());
    }

    private CompletableFuture<Void> sendUnMuteLogMessage(UnMuteLog muteLogModel, AServer server)  {
        MuteListenerModel model = MuteListenerModel
                .builder()
                .mutedUser(muteLogModel.getUnMutedUser())
                .mutingUser(muteLogModel.getMutingUser())
                .oldMuteTargetDate(muteLogModel.getMute() != null ? muteLogModel.getMute().getMuteTargetDate() : null)
                .muteTargetDate(null)
                .build();
        if(muteLogModel.getMute() != null) {
            log.debug("Sending unMute log for mute {} to the mute posttarget in server {}", muteLogModel.getMute().getMuteId().getId(), server.getId());
        }
        MessageToSend message = templateService.renderEmbedTemplate(MUTE_LOG_TEMPLATE, model, server.getId());
        return FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(message, MutingPostTarget.MUTE_LOG, server.getId()));
    }

    @Override
    @Transactional
    public CompletableFuture<Void> unMuteUser(ServerUser userToUnmute, ServerUser unMutingUser, Guild guild) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(userToUnmute);
        boolean muteActive = muteManagementService.hasActiveMute(aUserInAServer);
        if(!muteActive) {
            return memberService.removeTimeout(guild, userToUnmute, null)
                    .thenCompose(unused -> self.sendUnmuteLog(null, guild, userToUnmute, unMutingUser));
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
                .thenCompose(unused -> self.sendUnmuteLog(muteId, guild, mutedUser, mutingUser));
    }


    @Transactional
    public CompletableFuture<Void> sendUnmuteLog(Long muteId, Guild guild, ServerUser unMutedMember, ServerUser mutingMember) {
        Mute mute = null;
        if(muteId != null) {
            mute = muteManagementService.findMute(muteId, guild.getIdLong());
        }
        AServer mutingServer = serverManagementService.loadServer(guild.getIdLong());
        UnMuteLog unMuteLog = UnMuteLog
                .builder()
                .mute(mute)
                .mutingUser(MemberDisplay.fromServerUser(mutingMember))
                .unMutedUser(MemberDisplay.fromServerUser(unMutedMember))
                .build();
        CompletableFuture<Void> notificationFuture = sendUnMuteLogMessage(unMuteLog, mutingServer);
        return CompletableFuture.allOf(notificationFuture).thenAccept(aVoid -> {
            if(muteId != null) {
                self.endMuteInDatabase(muteId, guild.getIdLong());
            }
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

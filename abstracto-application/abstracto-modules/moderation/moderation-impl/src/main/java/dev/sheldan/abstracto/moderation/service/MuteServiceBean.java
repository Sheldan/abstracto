package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
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
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.database.Mute;
import dev.sheldan.abstracto.moderation.model.template.command.MuteContext;
import dev.sheldan.abstracto.moderation.model.template.command.MuteListenerModel;
import dev.sheldan.abstracto.moderation.model.template.command.MuteNotification;
import dev.sheldan.abstracto.moderation.model.template.command.UnMuteLog;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
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
    private ChannelService channelService;

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
    public CompletableFuture<Void> muteMember(Member memberToMute, String reason, Instant unMuteDate, Long channelId) {
        FullUserInServer mutedUser = FullUserInServer
                .builder()
                    .aUserInAServer(userInServerManagementService.loadOrCreateUser(memberToMute))
                .member(memberToMute)
                .build();
        return muteUserInServer(mutedUser, reason, unMuteDate, channelId);
    }

    @Override
    public CompletableFuture<Void> muteUserInServer(FullUserInServer userBeingMuted, String reason, Instant unMuteDate, Long channelId) {
        Member memberBeingMuted = userBeingMuted.getMember();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        futures.add(memberService.timeoutUser(userBeingMuted.getMember(), unMuteDate));
        Guild guild = memberBeingMuted.getGuild();
        if(memberBeingMuted.getVoiceState() != null && memberBeingMuted.getVoiceState().getChannel() != null) {
            futures.add(guild.kickVoiceMember(memberBeingMuted).submit());
        }
        MuteNotification muteNotification = MuteNotification
                .builder()
                .muteTargetDate(unMuteDate)
                .reason(reason)
                .serverName(guild.getName())
                .build();
        futures.add(sendMuteNotification(memberBeingMuted, muteNotification, channelId));
        return FutureUtils.toSingleFutureGeneric(futures);
    }

    private CompletableFuture<Void> sendMuteNotification(Member memberBeingMuted, MuteNotification muteNotification, Long channelId) {
        log.info("Notifying the user about the mute.");
        CompletableFuture<Void> notificationFuture = new CompletableFuture<>();
        Long guildId = memberBeingMuted.getGuild().getIdLong();
        String muteNotificationMessage = templateService.renderTemplate(MUTE_NOTIFICATION_TEMPLATE, muteNotification, guildId);
        CompletableFuture<Message> messageCompletableFuture = messageService.sendMessageToUser(memberBeingMuted.getUser(), muteNotificationMessage);
        messageCompletableFuture.exceptionally(throwable -> {
            GuildMessageChannel feedBackChannel = channelService.getMessageChannelFromServer(guildId, channelId);
            channelService.sendTextToChannel(throwable.getMessage(), feedBackChannel).whenComplete((exceptionMessage, innerThrowable) -> {
                notificationFuture.complete(null);
                log.info("Successfully notified user {} in server {} about mute.", memberBeingMuted.getId(), memberBeingMuted.getGuild().getId());
            }).exceptionally(throwable1 -> {
                notificationFuture.completeExceptionally(throwable1);
                return null;
            });
            return null;
        });
        messageCompletableFuture.thenAccept(message1 ->
            notificationFuture.complete(null)
        );
        return notificationFuture;
    }

    private void createMuteObject(MuteContext muteContext, String triggerKey, Long infractionId) {
        AChannel channel = channelManagementService.loadChannel(muteContext.getChannelId());
        AServerAChannelMessage origin = AServerAChannelMessage
                .builder()
                .channel(channel)
                .server(channel.getServer())
                .build();
        AUserInAServer userInServerBeingMuted = userInServerManagementService.loadOrCreateUser(muteContext.getMutedUser());
        AUserInAServer userInServerMuting = userInServerManagementService.loadOrCreateUser(muteContext.getMutingUser());
        muteManagementService.createMute(userInServerBeingMuted, userInServerMuting, muteContext.getReason(), muteContext.getMuteTargetDate(), origin, triggerKey, muteContext.getMuteId(), infractionId);
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
    public CompletableFuture<Void> muteMemberWithLog(MuteContext context) {
        log.debug("Muting member {} in server {}.", context.getMutedUser().getId(), context.getMutedUser().getGuild().getId());
        AServer server = serverManagementService.loadOrCreate(context.getMutedUser().getGuild().getIdLong());
        Long nextCounterValue = counterService.getNextCounterValue(server, MUTE_COUNTER_KEY);
        context.setMuteId(nextCounterValue);
        return muteMember(context.getMutedUser(), context.getReason(), context.getMuteTargetDate(), context.getChannelId())
                .thenCompose(unused -> self.sendMuteLog(context))
                .thenCompose(logMessage -> self.evaluateAndStoreInfraction(context, logMessage))
                .thenAccept(infractionId -> self.persistMute(context, infractionId));
    }

    @Transactional
    public CompletableFuture<Long> evaluateAndStoreInfraction(MuteContext context, Message logMessage) {
        Guild guild = context.getMutedUser().getGuild();
        if(featureFlagService.getFeatureFlagValue(ModerationFeatureDefinition.INFRACTIONS, guild.getIdLong())) {
            Long infractionPoints = configService.getLongValueOrConfigDefault(MutingFeatureConfig.MUTE_INFRACTION_POINTS, guild.getIdLong());
            AUserInAServer mutedUser = userInServerManagementService.loadOrCreateUser(context.getMutedUser());
            AUserInAServer mutingUser = userInServerManagementService.loadOrCreateUser(context.getMutingUser());
            Map<String, String> parameters = new HashMap<>();
            parameters.put(INFRACTION_PARAMETER_DURATION_KEY, templateService.renderDuration(Duration.between(Instant.now(), context.getMuteTargetDate()), guild.getIdLong()));
            return infractionService.createInfractionWithNotification(mutedUser, infractionPoints, MUTE_INFRACTION_TYPE, context.getReason(), mutingUser, parameters, logMessage)
                    .thenApply(Infraction::getId);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Transactional
    public void persistMute(MuteContext context, Long infractionId) {
        completelyUnMuteMember(context.getMutedUser());
        String triggerKey = startUnMuteJobFor(context.getMuteTargetDate(), context.getMuteId(), context.getMutedUser().getGuild().getIdLong());
        createMuteObject(context, triggerKey, infractionId);
    }

    @Transactional
    public CompletableFuture<Message> sendMuteLog(MuteContext muteLogModel)  {
        MuteListenerModel model = MuteListenerModel
                .builder()
                .mutedUser(muteLogModel.getMutedUser())
                .mutingUser(muteLogModel.getMutingUser())
                .channelId(muteLogModel.getChannelId())
                .oldMuteTargetDate(null)
                .muteTargetDate(muteLogModel.getMuteTargetDate())
                .reason(muteLogModel.getReason())
                .build();
        log.debug("Sending mute log to the mute post target.");
        MessageToSend message = templateService.renderEmbedTemplate(MUTE_LOG_TEMPLATE, model, muteLogModel.getMutedUser().getIdLong());
        List<CompletableFuture<Message>> futures = postTargetService.sendEmbedInPostTarget(message, MutingPostTarget.MUTE_LOG, muteLogModel.getMutedUser().getGuild().getIdLong());
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
    public CompletableFuture<Void> unMuteUser(AUserInAServer userToUnmute, Member unMutingMember) {
        boolean muteActive = muteManagementService.hasActiveMute(userToUnmute);
        if(!muteActive) {
            CompletableFuture<Member> unMutedMemberFuture = memberService.retrieveMemberInServer(ServerUser.fromAUserInAServer(userToUnmute));
            return unMutedMemberFuture
                    .thenCompose(member -> memberService.removeTimeout(member))
                    .thenCompose(unused -> self.sendUnmuteLog(null, unMutingMember.getGuild(), unMutedMemberFuture.join(), unMutingMember));
        } else {
            Mute mute = muteManagementService.getAMuteOf(userToUnmute);
            return endMute(mute);
        }
    }

    @Override
    public CompletableFuture<Void> endMute(Mute mute) {
        if(mute.getMuteEnded()) {
            log.info("Mute {} in server {} has already ended. Not unmuting.", mute.getMuteId().getId(), mute.getMuteId().getServerId());
            return CompletableFuture.completedFuture(null);
        }
        Long muteId = mute.getMuteId().getId();
        Guild guild = guildService.getGuildById(mute.getMuteId().getServerId());
        AServer mutingServer = mute.getServer();
        log.info("UnMuting {} in server {}", mute.getMutedUser().getUserReference().getId(), mutingServer.getId());
        CompletableFuture<Member> mutedMemberFuture = memberService.getMemberInServerAsync(mute.getMutedUser());
        CompletableFuture<Member> mutingMemberFuture = memberService.getMemberInServerAsync(mute.getMutingUser());
        return CompletableFuture.allOf(mutedMemberFuture, mutingMemberFuture)
                .thenAccept(member -> memberService.removeTimeout(mutedMemberFuture.join()))
                .thenCompose(unused -> self.sendUnmuteLog(muteId, guild, mutingMemberFuture, mutedMemberFuture));
    }

    @Transactional
    public CompletableFuture<Void> sendUnmuteLog(Long muteId, Guild guild, CompletableFuture<Member> mutingMemberFuture, CompletableFuture<Member> mutedMemberFuture) {
        Member mutingMember = !mutingMemberFuture.isCompletedExceptionally() ? mutingMemberFuture.join() : null;
        Member mutedMember = !mutedMemberFuture.isCompletedExceptionally() ? mutedMemberFuture.join() : null;
        return sendUnmuteLog(muteId, guild, mutedMember, mutingMember);
    }

    @Transactional
    public CompletableFuture<Void> sendUnmuteLog(Long muteId, Guild guild, Member mutedMember, Member mutingMember) {
        Mute mute = null;
        if(muteId != null) {
            mute = muteManagementService.findMute(muteId, guild.getIdLong());
        }
        AServer mutingServer = serverManagementService.loadServer(guild.getIdLong());
        UnMuteLog unMuteLog = UnMuteLog
                .builder()
                .mute(mute)
                .mutingUser(mutingMember)
                .unMutedUser(mutedMember)
                .guild(guild)
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
            return endMute(muteOptional.get());
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
    public void completelyUnMuteMember(Member member) {
        completelyUnMuteUser(userInServerManagementService.loadOrCreateUser(member));
    }

}

package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.config.features.MutingMode;
import dev.sheldan.abstracto.moderation.config.posttargets.MutingPostTarget;
import dev.sheldan.abstracto.moderation.exception.MuteRoleNotSetupException;
import dev.sheldan.abstracto.moderation.exception.NoMuteFoundException;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import dev.sheldan.abstracto.moderation.models.database.MuteRole;
import dev.sheldan.abstracto.moderation.models.template.commands.MuteContext;
import dev.sheldan.abstracto.moderation.models.template.commands.MuteNotification;
import dev.sheldan.abstracto.moderation.models.template.commands.UnMuteLog;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import dev.sheldan.abstracto.moderation.service.management.MuteRoleManagementService;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MuteServiceBean implements MuteService {

    @Autowired
    private MuteRoleManagementService muteRoleManagementService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private MuteManagementService muteManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private BotService botService;

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
    private FeatureModeService featureModeService;

    public static final String MUTE_LOG_TEMPLATE = "mute_log";
    public static final String UN_MUTE_LOG_TEMPLATE = "unmute_log";
    public static final String MUTE_NOTIFICATION_TEMPLATE = "mute_notification";
    public static final String MUTE_COUNTER_KEY = "MUTES";

    @Override
    public CompletableFuture<Void> muteMember(Member memberToMute, Member mutingMember, String reason, Instant unMuteDate, ServerChannelMessage message) {
        FullUserInServer mutedUser = FullUserInServer
                .builder()
                    .aUserInAServer(userInServerManagementService.loadUser(memberToMute))
                .member(memberToMute)
                .build();

        FullUserInServer mutingUser = FullUserInServer
                .builder()
                .aUserInAServer(userInServerManagementService.loadUser(mutingMember))
                .member(mutingMember)
                .build();
        return muteUserInServer(mutedUser, mutingUser, reason, unMuteDate, message);
    }

    @Override
    public CompletableFuture<Void> muteUserInServer(FullUserInServer userBeingMuted, FullUserInServer userMuting, String reason, Instant unMuteDate, ServerChannelMessage message) {
        AServer serverBeingMutedIn = userBeingMuted.getAUserInAServer().getServerReference();
        if(!muteRoleManagementService.muteRoleForServerExists(serverBeingMutedIn)) {
            log.error("Mute role for server {} has not been setup.", serverBeingMutedIn.getId());
            throw new MuteRoleNotSetupException();
        }
        Member memberBeingMuted = userBeingMuted.getMember();
        log.info("User {} mutes user {} in server {} until {}",
                memberBeingMuted.getIdLong(), message.getServerId(), userMuting.getMember().getIdLong(), unMuteDate);
        if(message.getMessageId() != null) {
            log.info("because of message {} in channel {} in server {}", message.getMessageId(), message.getChannelId(), message.getServerId());
        } else {
            log.info("This mute was not triggered by a message.");
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AUserInAServer userInServerBeingMuted = userBeingMuted.getAUserInAServer();
        futures.add(applyMuteRole(userInServerBeingMuted));
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
        futures.add(sendMuteNotification(message, memberBeingMuted, muteNotification));
        return FutureUtils.toSingleFutureGeneric(futures);
    }

    private CompletableFuture<Void> sendMuteNotification(ServerChannelMessage message, Member memberBeingMuted, MuteNotification muteNotification) {
        log.info("Notifying the user about the mute.");
        CompletableFuture<Void> notificationFuture = new CompletableFuture<>();
        String muteNotificationMessage = templateService.renderTemplate(MUTE_NOTIFICATION_TEMPLATE, muteNotification);
        CompletableFuture<Message> messageCompletableFuture = messageService.sendMessageToUser(memberBeingMuted.getUser(), muteNotificationMessage);
        messageCompletableFuture.exceptionally(throwable -> {
            TextChannel feedBackChannel = botService.getTextChannelFromServer(message.getServerId(), message.getChannelId());
            feedBackChannel.sendMessage(throwable.getMessage()).submit().whenComplete((exceptionMessage, innerThrowable) -> {
                notificationFuture.complete(null);
                log.info("Successfully notified user {} in server {} about mute.", memberBeingMuted.getId(), memberBeingMuted.getGuild().getId());
            });
            return null;
        });
        messageCompletableFuture.thenAccept(message1 ->
            notificationFuture.complete(null)
        );
        return notificationFuture;
    }

    private void createMuteObject(MuteContext muteContext, String triggerKey) {
        AChannel channel = channelManagementService.loadChannel(muteContext.getContext().getChannelId());
        AServerAChannelMessage origin = AServerAChannelMessage
                .builder()
                .channel(channel)
                .server(channel.getServer())
                .messageId(muteContext.getContext().getMessageId())
                .build();
        AUserInAServer userInServerBeingMuted = userInServerManagementService.loadUser(muteContext.getMutedUser());
        AUserInAServer userInServerMuting = userInServerManagementService.loadUser(muteContext.getMutedUser());
        muteManagementService.createMute(userInServerBeingMuted, userInServerMuting, muteContext.getReason(), muteContext.getMuteTargetDate(), origin, triggerKey, muteContext.getMuteId());
    }

    @Override
    public CompletableFuture<Void> applyMuteRole(AUserInAServer aUserInAServer) {
        MuteRole muteRole = muteRoleManagementService.retrieveMuteRoleForServer(aUserInAServer.getServerReference());
        return roleService.addRoleToUserFuture(aUserInAServer, muteRole.getRole());
    }

    @Override
    public String startUnMuteJobFor(Instant unMuteDate, Long muteId, Long serverId) {
        Duration muteDuration = Duration.between(Instant.now(), unMuteDate);
        if(muteDuration.getSeconds() < 60) {
            log.trace("Directly scheduling the unMute, because it was below the threshold.");
            unMuteScheduler.schedule(() -> {
                try {
                    self.endMute(muteId, serverId);
                } catch (Exception exception) {
                    log.error("Failed to remind immediately.", exception);
                }
            }, muteDuration.toNanos(), TimeUnit.NANOSECONDS);
            return null;
        } else {
            log.trace("Starting scheduled job to execute unMute.");
            JobDataMap parameters = new JobDataMap();
            parameters.putAsString("muteId", muteId);
            parameters.putAsString("serverId", serverId);
            return schedulerService.executeJobWithParametersOnce("unMuteJob", "moderation", parameters, Date.from(unMuteDate));
        }
    }

    @Override
    public void cancelUnMuteJob(Mute mute) {
        if(mute.getTriggerKey() != null) {
            schedulerService.stopTrigger(mute.getTriggerKey());
        }
    }

    @Override
    public CompletableFuture<Void> muteMemberWithLog(MuteContext context) {
        log.trace("Muting member {} in server {}.", context.getMutedUser().getId(), context.getMutedUser().getGuild().getId());
        AServer server = serverManagementService.loadOrCreate(context.getContext().getServerId());
        Long nextCounterValue = counterService.getNextCounterValue(server, MUTE_COUNTER_KEY);
        context.setMuteId(nextCounterValue);
        CompletableFuture<Void> mutingFuture = muteMember(context.getMutedUser(), context.getMutingUser(), context.getReason(), context.getMuteTargetDate(), context.getContext());
        CompletableFuture<Void> muteLogFuture = sendMuteLog(context, server);
        return CompletableFuture.allOf(mutingFuture, muteLogFuture).thenAccept(aVoid ->
            self.persistMute(context)
        );
    }

    @Transactional
    public void persistMute(MuteContext context) {
        String triggerKey = startUnMuteJobFor(context.getMuteTargetDate(), context.getMuteId(), context.getContext().getServerId());
        createMuteObject(context, triggerKey);
    }

    private CompletableFuture<Void> sendMuteLog(MuteContext muteLogModel, AServer server)  {
        CompletableFuture<Void> completableFuture;
        if(featureModeService.featureModeActive(ModerationFeatures.MUTING, server, MutingMode.MUTE_LOGGING)) {
            log.trace("Sending mute log to the mute post target.");
            MessageToSend message = templateService.renderEmbedTemplate(MUTE_LOG_TEMPLATE, muteLogModel);
            List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(message, MutingPostTarget.MUTE_LOG, muteLogModel.getContext().getServerId());
            completableFuture = FutureUtils.toSingleFutureGeneric(completableFutures);
        } else {
            completableFuture = CompletableFuture.completedFuture(null);
            log.trace("Not sending mute log, because feature mode {} in feature {} has been disabled for server {}.", MutingMode.MUTE_LOGGING, ModerationFeatures.WARNING, server.getId());
        }
        return completableFuture;
    }

    private CompletableFuture<Void> sendUnMuteLogMessage(UnMuteLog muteLogModel, AServer server)  {
        CompletableFuture<Void> completableFuture;
        if(featureModeService.featureModeActive(ModerationFeatures.MUTING, server, MutingMode.MUTE_LOGGING)) {
            log.trace("Sending unMute log for mute {} to the mute posttarget in server {}", muteLogModel.getMute().getMuteId().getId(), server.getId());
            MessageToSend message = templateService.renderEmbedTemplate(UN_MUTE_LOG_TEMPLATE, muteLogModel);
            List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(message, MutingPostTarget.MUTE_LOG, muteLogModel.getServer().getId());
            completableFuture = FutureUtils.toSingleFutureGeneric(completableFutures);
        } else {
            completableFuture = CompletableFuture.completedFuture(null);
            log.trace("Not sending unMute log, because feature mode {} in feature {} has been disabled for server {}.", MutingMode.UN_MUTE_LOGGING, ModerationFeatures.WARNING, server.getId());
        }
        return completableFuture;
    }

    @Override
    @Transactional
    public CompletableFuture<Void> unMuteUser(AUserInAServer aUserInAServer) {
        if(!muteManagementService.hasActiveMute(aUserInAServer)) {
            throw new NoMuteFoundException();
        }
        Mute mute = muteManagementService.getAMuteOf(aUserInAServer);
        if(Boolean.TRUE.equals(mute.getMuteEnded())) {
            log.info("Mute {} has ended already, user {} does not need to be unMuted anymore.", mute.getMuteId().getId(), mute.getMutedUser().getUserReference().getId());
            return CompletableFuture.completedFuture(null);
        }
        Long muteId = mute.getMuteId().getId();
        CompletableFuture<Member> mutingMemberFuture = botService.getMemberInServerAsync(mute.getMutingUser());
        CompletableFuture<Member> mutedMemberFuture = botService.getMemberInServerAsync(mute.getMutedUser());
        Guild guild = botService.getGuildById(mute.getServer().getId());
        return endMute(mute).thenCompose(unused ->
            CompletableFuture.allOf(mutingMemberFuture, mutedMemberFuture)
        ).thenCompose(unused -> self.sendUnMuteLogForManualUnMute(muteId, mutingMemberFuture, mutedMemberFuture, guild));
    }

    @Transactional
    public CompletableFuture<Void> sendUnMuteLogForManualUnMute(Long muteId, CompletableFuture<Member> mutingMemberFuture, CompletableFuture<Member> mutedMemberFuture, Guild guild) {
        CompletableFuture<Void> completableFuture;
        if(featureModeService.featureModeActive(ModerationFeatures.MUTING, guild.getIdLong(), MutingMode.MANUAL_UN_MUTE_LOGGING)) {
            completableFuture = self.sendUnmuteLog(muteId, guild, mutingMemberFuture, mutedMemberFuture);
            log.trace("Sending un mute notification for manual un mute for mute {} in server {}.", muteId, guild.getIdLong());
        } else {
            completableFuture = CompletableFuture.completedFuture(null);
            log.trace("Not sending unMute log, because feature mode {} in feature {} has been disabled for server {}.", MutingMode.MANUAL_UN_MUTE_LOGGING, ModerationFeatures.WARNING, guild.getIdLong());

        }
        return completableFuture;
    }

    @Override
    public CompletableFuture<Void> endMute(Mute mute) {
        Long muteId = mute.getMuteId().getId();
        AServer mutingServer = mute.getServer();
        log.info("UnMuting {} in server {}", mute.getMutedUser().getUserReference().getId(), mutingServer.getId());
        MuteRole muteRole = muteRoleManagementService.retrieveMuteRoleForServer(mutingServer);
        log.trace("Using the mute role {} mapping to role {}", muteRole.getId(), muteRole.getRole().getId());
        Guild guild = botService.getGuildById(mutingServer.getId());
        CompletableFuture<Void> roleRemovalFuture;
        // TODO replace with future, because caching
        if(botService.isUserInGuild(guild, mute.getMutedUser())) {
            roleRemovalFuture = roleService.removeRoleFromUserFuture(mute.getMutedUser(), muteRole.getRole());
        } else {
            roleRemovalFuture = CompletableFuture.completedFuture(null);
            log.info("User to unMute left the guild.");
        }
        CompletableFuture<Member> mutingMemberFuture = botService.getMemberInServerAsync(mute.getMutingUser());
        CompletableFuture<Member> mutedMemberFuture = botService.getMemberInServerAsync(mute.getMutedUser());
        CompletableFuture<Void> finalFuture = new CompletableFuture<>();
        CompletableFuture.allOf(mutingMemberFuture, mutedMemberFuture, roleRemovalFuture, mutingMemberFuture, mutedMemberFuture).handle((aVoid, throwable) -> {
            self.sendUnmuteLog(muteId, guild, mutingMemberFuture, mutedMemberFuture).thenAccept(aVoid1 ->
                finalFuture.complete(null)
            );
            return null;
        });

        return finalFuture;
    }

    @Transactional
    public CompletableFuture<Void> sendUnmuteLog(Long muteId, Guild guild, CompletableFuture<Member> mutingMemberFuture, CompletableFuture<Member> mutedMemberFuture) {
        Mute mute = muteManagementService.findMute(muteId, guild.getIdLong());
        AServer mutingServer = serverManagementService.loadServer(guild.getIdLong());
        UnMuteLog unMuteLog = UnMuteLog
                .builder()
                .mute(mute)
                .mutingUser(mutingMemberFuture.join())
                .unMutedUser(mutedMemberFuture.join())
                .guild(guild)
                .server(mutingServer)
                .build();
        CompletableFuture<Void> notificationFuture = sendUnMuteLogMessage(unMuteLog, mutingServer);
        return CompletableFuture.allOf(notificationFuture).thenAccept(aVoid ->
            self.endMuteInDatabase(muteId, guild.getIdLong())
        );
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
        List<Mute> allMutesOfUser = muteManagementService.getAllMutesOf(aUserInAServer);
        allMutesOfUser.forEach(mute -> {
            mute.setMuteEnded(true);
            cancelUnMuteJob(mute);
            muteManagementService.saveMute(mute);
        });
    }

    @Override
    public void completelyUnMuteMember(Member member) {
        completelyUnMuteUser(userInServerManagementService.loadUser(member));
    }
}

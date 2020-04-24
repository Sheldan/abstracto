package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.moderation.exception.MuteException;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import dev.sheldan.abstracto.moderation.models.database.MuteRole;
import dev.sheldan.abstracto.moderation.models.template.commands.MuteLog;
import dev.sheldan.abstracto.moderation.models.template.commands.MuteNotification;
import dev.sheldan.abstracto.moderation.models.template.commands.UnMuteLog;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import dev.sheldan.abstracto.moderation.service.management.MuteRoleManagementService;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.Executors;
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
    private UserManagementService userManagementService;

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
    private MuteService self;

    @Autowired
    private ChannelManagementService channelManagementService;

    private static final String MUTE_LOG_TEMPLATE = "mute_log";
    private static final String UNMUTE_LOG_TEMPLATE = "unmute_log";
    private static final String MUTE_LOG_TARGET = "muteLog";
    private static final String MUTE_NOTIFICATION_TEMPLATE = "mute_notification";

    @Override
    public Mute muteMember(Member memberToMute, Member mutingMember, String reason, Instant unmuteDate, Message message) {
        FullUser mutedUser = FullUser
                .builder()
                .aUserInAServer(userManagementService.loadUser(memberToMute))
                .member(memberToMute)
                .build();

        FullUser mutingUser = FullUser
                .builder()
                .aUserInAServer(userManagementService.loadUser(memberToMute))
                .member(mutingMember)
                .build();
        return muteUser(mutedUser, mutingUser, reason, unmuteDate, message);
    }

    @Override
    public Mute muteMember(AUserInAServer userBeingMuted, AUserInAServer userMuting, String reason, Instant unmuteDate, Message message) {
        FullUser mutedUser = FullUser
                .builder()
                .aUserInAServer(userBeingMuted)
                .member(botService.getMemberInServer(userBeingMuted))
                .build();

        FullUser mutingUser = FullUser
                .builder()
                .aUserInAServer(userMuting)
                .member(botService.getMemberInServer(userMuting))
                .build();
        return muteUser(mutedUser, mutingUser, reason, unmuteDate, message);
    }

    @Override
    public Mute muteUser(FullUser userBeingMuted, FullUser userMuting, String reason, Instant unmuteDate, Message message) {
        log.info("User {} mutes {} until {}",
                userBeingMuted.getMember().getIdLong(), userMuting.getMember().getIdLong(), unmuteDate);
        if(message != null) {
            log.trace("because of message {} in channel {} in server {}", message.getId(), message.getChannel().getId(), message.getGuild().getId());
        } else {
            log.trace("This mute was not triggered by a message.");
        }
        if(!muteRoleManagementService.muteRoleForServerExists(userBeingMuted.getAUserInAServer().getServerReference())) {
            log.error("Mute role for server {} has not been setup.", userBeingMuted.getAUserInAServer().getServerReference().getId());
            throw new MuteException("Mute role for server has not been setup");
        }
        AUserInAServer userInServerBeingMuted = userBeingMuted.getAUserInAServer();
        MuteRole muteRole = muteRoleManagementService.retrieveMuteRoleForServer(userInServerBeingMuted.getServerReference());
        roleService.addRoleToUser(userInServerBeingMuted, muteRole.getRole());
        AServerAChannelMessage origin = null;
        if(message != null) {
            AChannel channel = channelManagementService.loadChannel(message.getChannel().getIdLong());
            origin = AServerAChannelMessage
                    .builder()
                    .channel(channel)
                    .server(channel.getServer())
                    .messageId(message.getIdLong())
                    .build();
        }
        Mute mute = muteManagementService.createMute(userInServerBeingMuted, userMuting.getAUserInAServer(), reason, unmuteDate, origin);

        log.trace("Notifying the user about the mute.");
        MuteNotification muteNotification = MuteNotification.builder().mute(mute).serverName(userBeingMuted.getMember().getGuild().getName()).build();
        String muteNotificationMessage = templateService.renderTemplate(MUTE_NOTIFICATION_TEMPLATE, muteNotification);
        TextChannel textChannel = message != null ? message.getTextChannel() : null;
        messageService.sendMessageToUser(userBeingMuted.getMember().getUser(), muteNotificationMessage, textChannel);

        startUnmuteJobFor(unmuteDate, mute);
        return mute;
    }

    @Override
    public void startUnmuteJobFor(Instant unmuteDate, Mute mute) {
        Duration muteDuration = Duration.between(Instant.now(), unmuteDate);
        if(muteDuration.getSeconds() < 60) {
            log.trace("Directly scheduling the unmute, because it was below the threshold.");
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.schedule(() -> {
                try {
                    self.unmuteUser(mute);
                } catch (Exception exception) {
                    log.error("Failed to remind immediately.", exception);
                }
            }, muteDuration.toNanos(), TimeUnit.NANOSECONDS);
        } else {
            log.trace("Starting scheduled job to execute unmute.");
            JobDataMap parameters = new JobDataMap();
            parameters.putAsString("muteId", mute.getId());
            schedulerService.executeJobWithParametersOnce("unMuteJob", "moderation", parameters, Date.from(unmuteDate));
        }
    }

    @Override
    public void muteMemberWithLog(Member memberToMute, Member memberMuting, String reason, Instant unmuteDate, MuteLog muteLog, Message message) {
        log.trace("Muting member with sending a mute log");
        Mute mute = muteMember(memberToMute, memberMuting, reason, unmuteDate, message);
        muteLog.setMute(mute);
        sendMuteLog(muteLog);
    }

    private void sendMuteLog(MuteLog muteLogModel)  {
        log.trace("Sending mute log to the mute posttarget");
        MessageToSend message = templateService.renderEmbedTemplate(MUTE_LOG_TEMPLATE, muteLogModel);
        postTargetService.sendEmbedInPostTarget(message, MUTE_LOG_TARGET, muteLogModel.getServer().getId());
    }

    private void sendUnmuteLog(UnMuteLog muteLogModel)  {
        log.trace("Sending unmute log to the mute posttarget");
        MessageToSend message = templateService.renderEmbedTemplate(UNMUTE_LOG_TEMPLATE, muteLogModel);
        postTargetService.sendEmbedInPostTarget(message, MUTE_LOG_TARGET, muteLogModel.getServer().getId());
    }

    @Override
    @Transactional
    public void unmuteUser(Mute mute) {
        AServer mutingServer = mute.getMutingServer();
        log.info("Unmuting {} in server {}", mutingServer.getId(), mute.getMutedUser().getUserReference().getId());
        MuteRole muteRole = muteRoleManagementService.retrieveMuteRoleForServer(mutingServer);
        log.trace("Using the mute role {} mapping to role {}", muteRole.getId(), muteRole.getRole().getId());
        roleService.removeRoleFromUser(mute.getMutedUser(), muteRole.getRole());
        UnMuteLog unMuteLog = UnMuteLog
                .builder()
                .mute(mute)
                .mutingUser(botService.getMemberInServer(mute.getMutingUser()))
                .unMutedUser(botService.getMemberInServer(mute.getMutedUser()))
                .guild(botService.getGuildById(mute.getMutingServer().getId()).orElseGet(null))
                .server(mute.getMutingServer())
                .build();
        sendUnmuteLog(unMuteLog);
    }

    @Override
    @Transactional
    public void endMute(Long muteId) {
        log.info("Unmuting the mute {}", muteId);
        Mute mute = muteManagementService.findMute(muteId);
        unmuteUser(mute);
    }
}

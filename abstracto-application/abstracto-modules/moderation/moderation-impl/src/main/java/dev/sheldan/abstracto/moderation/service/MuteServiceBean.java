package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
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
import net.dv8tion.jda.api.entities.Guild;
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
import java.util.List;
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
                .aUserInAServer(userInServerManagementService.loadUser(memberToMute))
                .member(memberToMute)
                .build();

        FullUser mutingUser = FullUser
                .builder()
                .aUserInAServer(userInServerManagementService.loadUser(mutingMember))
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
        Member memberBeingMuted = userBeingMuted.getMember();
        log.info("User {} mutes {} until {}",
                memberBeingMuted.getIdLong(), userMuting.getMember().getIdLong(), unmuteDate);
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
        applyMuteRole(userInServerBeingMuted);
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
        Guild guild = memberBeingMuted.getGuild();
        if(memberBeingMuted.getVoiceState() != null && memberBeingMuted.getVoiceState().getChannel() != null) {
            guild.kickVoiceMember(memberBeingMuted).queue();
        }
        log.trace("Notifying the user about the mute.");
        MuteNotification muteNotification = MuteNotification.builder().mute(mute).serverName(guild.getName()).build();
        String muteNotificationMessage = templateService.renderTemplate(MUTE_NOTIFICATION_TEMPLATE, muteNotification);
        TextChannel textChannel = message != null ? message.getTextChannel() : null;
        messageService.sendMessageToUser(memberBeingMuted.getUser(), muteNotificationMessage, textChannel);

        String triggerKey = startUnmuteJobFor(unmuteDate, mute);
        mute.setTriggerKey(triggerKey);
        muteManagementService.saveMute(mute);
        return mute;
    }

    @Override
    public void applyMuteRole(AUserInAServer aUserInAServer) {
        MuteRole muteRole = muteRoleManagementService.retrieveMuteRoleForServer(aUserInAServer.getServerReference());
        roleService.addRoleToUser(aUserInAServer, muteRole.getRole());
    }

    @Override
    public String startUnmuteJobFor(Instant unmuteDate, Mute mute) {
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
            return null;
        } else {
            log.trace("Starting scheduled job to execute unmute.");
            JobDataMap parameters = new JobDataMap();
            parameters.putAsString("muteId", mute.getId());
            return schedulerService.executeJobWithParametersOnce("unMuteJob", "moderation", parameters, Date.from(unmuteDate));
        }
    }

    @Override
    public void cancelUnmuteJob(Mute mute) {
        if(mute.getTriggerKey() != null) {
            schedulerService.stopTrigger(mute.getTriggerKey());
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
        Mute updatedMute = muteManagementService.findMute(mute.getId());
        // we do not store any reference to the instant unmutes (<=60sec), so we cannot cancel it
        // but if the person gets unmuted immediately, via command, this might still execute of the instant unmute
        // so we need to load the mute, and check if the mute was unmuted already, because the mute object we have at
        // hand was loaded earlier, and does not reflect the true state
        if(updatedMute.getMuteEnded()) {
            log.info("Mute {} has ended already, {} does not need to be unmuted anymore.", mute.getId(), mute.getMutedUser().getUserReference().getId());
            return;
        }
        log.info("Unmuting {} in server {}", mutingServer.getId(), mute.getMutedUser().getUserReference().getId());
        MuteRole muteRole = muteRoleManagementService.retrieveMuteRoleForServer(mutingServer);
        log.trace("Using the mute role {} mapping to role {}", muteRole.getId(), muteRole.getRole().getId());
        Guild guild = botService.getGuildByIdNullable(mute.getMutingServer().getId());
        if(botService.isUserInGuild(guild, mute.getMutedUser())) {
            roleService.removeRoleFromUser(mute.getMutedUser(), muteRole.getRole());
        } else {
            log.info("User to unmute left the guild.");
        }
        UnMuteLog unMuteLog = UnMuteLog
                .builder()
                .mute(mute)
                .mutingUser(botService.getMemberInServer(mute.getMutingUser()))
                .unMutedUser(botService.getMemberInServer(mute.getMutedUser()))
                .guild(guild)
                .server(mute.getMutingServer())
                .build();
        sendUnmuteLog(unMuteLog);
        mute.setMuteEnded(true);
        muteManagementService.saveMute(mute);
    }

    @Override
    @Transactional
    public void endMute(Long muteId) {
        log.info("Unmuting the mute {}", muteId);
        Mute mute = muteManagementService.findMute(muteId);
        unmuteUser(mute);
    }

    @Override
    public void completelyUnmuteUser(AUserInAServer aUserInAServer) {
        List<Mute> allMutesOfUser = muteManagementService.getAllMutesOf(aUserInAServer);
        allMutesOfUser.forEach(mute -> {
            mute.setMuteEnded(true);
            cancelUnmuteJob(mute);
            muteManagementService.saveMute(mute);
        });
    }

    @Override
    public void completelyUnmuteUser(Member member) {
        completelyUnmuteUser(userInServerManagementService.loadUser(member));
    }
}

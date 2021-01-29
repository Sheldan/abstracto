package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.exception.ReminderNotFoundException;
import dev.sheldan.abstracto.utility.models.database.Reminder;
import dev.sheldan.abstracto.utility.models.template.commands.reminder.ExecutedReminderModel;
import dev.sheldan.abstracto.utility.service.management.ReminderManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RemindServiceBean implements ReminderService {

    public static final String REMINDER_TEMPLATE_TEXT = "remind_reminder";
    @Autowired
    private ReminderManagementService reminderManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GuildService guildService;

    @Autowired
    private RemindServiceBean self;

    @Autowired
    private ChannelService channelService;

    @Autowired
    @Qualifier("reminderScheduler")
    private ScheduledExecutorService instantReminderScheduler;


    @Override
    public Reminder createReminderInForUser(AUserInAServer user, String remindText, Duration remindIn, Message message) {
        AChannel channel = channelManagementService.loadChannel(message.getChannel().getIdLong());
        AServerAChannelAUser aServerAChannelAUser = AServerAChannelAUser
                .builder()
                .user(user.getUserReference())
                .aUserInAServer(user)
                .guild(user.getServerReference())
                .channel(channel)
                .build();
        Instant remindAt = Instant.now().plusNanos(remindIn.toNanos());
        Reminder reminder = reminderManagementService.createReminder(aServerAChannelAUser, remindText, remindAt, message.getIdLong());
        log.info("Creating reminder for user {} in guild {} due at {}.",
                user.getUserReference().getId(), user.getServerReference().getId(), remindAt);

        if(remindIn.getSeconds() < 60) {
            log.info("Directly scheduling unremind for reminder {}, because it was below the threshold.", reminder.getId());
            instantReminderScheduler.schedule(() -> {
                try {
                    self.executeReminder(reminder.getId());
                } catch (Exception exception) {
                    log.error("Failed to remind immediately.", exception);
                }
            }, remindIn.toNanos(), TimeUnit.NANOSECONDS);
        } else {
            JobDataMap parameters = new JobDataMap();
            parameters.putAsString("reminderId", reminder.getId());
            String triggerKey = schedulerService.executeJobWithParametersOnce("reminderJob", "utility", parameters, Date.from(reminder.getTargetDate()));
            log.info("Starting scheduled job  with trigger {} to execute reminder. {}", triggerKey, reminder.getId());
            reminder.setJobTriggerKey(triggerKey);
            reminderManagementService.saveReminder(reminder);
        }
        return reminder;
    }

    @Override
    @Transactional
    public void executeReminder(Long reminderId)  {
        Reminder reminderToRemindFor = reminderManagementService.loadReminder(reminderId);
        if(reminderToRemindFor.isReminded()) {
            return;
        }
        AServer server = reminderToRemindFor.getServer();
        AChannel channel = reminderToRemindFor.getChannel();
        log.info("Executing reminder {} in channel {} in server {} for user {}.",
                reminderId, channel.getId(), server.getId(), reminderToRemindFor.getRemindedUser().getUserReference().getId());
        Optional<Guild> guildToAnswerIn = guildService.getGuildByIdOptional(server.getId());
        if(guildToAnswerIn.isPresent()) {
            Optional<TextChannel> channelToAnswerIn = channelService.getTextChannelFromServerOptional(server.getId(), channel.getId());
            // only send the message if the channel still exists, if not, only set the reminder to reminded.
            if(channelToAnswerIn.isPresent()) {
                memberService.getMemberInServerAsync(server.getId(), reminderToRemindFor.getRemindedUser().getUserReference().getId()).thenAccept(member ->
                    self.sendReminderText(reminderId, channelToAnswerIn.get(), member)
                );

            } else {
                log.warn("Channel {} in server {} to remind user did not exist anymore. Ignoring reminder {}", channel.getId(), server.getId(), reminderId);
            }
        } else {
            log.warn("Guild {} to remind user in did not exist anymore. Ignoring reminder {}.", server.getId(), reminderId);
        }
        reminderManagementService.setReminded(reminderToRemindFor);
    }

    @Transactional
    public CompletableFuture<Void> sendReminderText(Long reminderId, TextChannel channelToAnswerIn, Member member) {
        Reminder reminder = reminderManagementService.loadReminder(reminderId);
        log.trace("Sending remind message for reminder {} to user user {} in server {}.", reminderId, member.getIdLong(), member.getGuild().getIdLong());
        ExecutedReminderModel build = ExecutedReminderModel
                .builder()
                .reminder(reminder)
                .member(member)
                .duration(Duration.between(reminder.getReminderDate(), reminder.getTargetDate()))
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(REMINDER_TEMPLATE_TEXT, build);
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, channelToAnswerIn));
    }

    @Override
    public void unRemind(Long reminderId, AUserInAServer aUserInAServer) {
        log.info("Trying to end reminder {} for user {} in server {}.", reminderId, aUserInAServer.getUserReference().getId(),aUserInAServer.getServerReference().getId());
        Reminder reminder = reminderManagementService.getReminderByAndByUserNotReminded(aUserInAServer, reminderId).orElseThrow(() -> new ReminderNotFoundException(reminderId));
        reminder.setReminded(true);
        if(reminder.getJobTriggerKey() != null) {
            log.trace("Stopping scheduled trigger {} for reminder {}.", reminder.getJobTriggerKey(), reminderId);
            schedulerService.stopTrigger(reminder.getJobTriggerKey());
        }
    }
}

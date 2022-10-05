package dev.sheldan.abstracto.remind.service;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.MemberNameDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.remind.exception.NotPossibleToSnoozeException;
import dev.sheldan.abstracto.remind.exception.ReminderNotFoundException;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import dev.sheldan.abstracto.remind.model.database.ReminderParticipant;
import dev.sheldan.abstracto.remind.model.template.commands.ExecutedReminderModel;
import dev.sheldan.abstracto.remind.service.management.ReminderManagementService;
import dev.sheldan.abstracto.remind.service.management.ReminderParticipantManagementService;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private ReminderParticipantManagementService reminderParticipantManagementService;

    @Autowired
    @Qualifier("reminderScheduler")
    private ScheduledExecutorService instantReminderScheduler;


    @Override
    public Reminder createReminderInForUser(AUserInAServer user, String remindText, Duration remindIn, Message message) {
        return createReminderInForUser(user, remindText, remindIn, message.getChannel().getIdLong(), message.getIdLong());
    }

    @Override
    public Reminder createReminderInForUser(AUserInAServer user, String remindText, Duration remindIn, Long channelId, Long messageId) {
        AChannel channel = channelManagementService.loadChannel(channelId);
        AServerAChannelAUser aServerAChannelAUser = AServerAChannelAUser
                .builder()
                .user(user.getUserReference())
                .aUserInAServer(user)
                .guild(user.getServerReference())
                .channel(channel)
                .build();
        Instant remindAt = Instant.now().plusMillis(remindIn.toMillis());
        Reminder reminder = reminderManagementService.createReminder(aServerAChannelAUser, remindText, remindAt, messageId);
        log.info("Creating reminder for user {} in guild {} due at {}.",
                user.getUserReference().getId(), user.getServerReference().getId(), remindAt);

        scheduleReminder(remindIn, reminder);
        return reminder;
    }

    @Override
    public Reminder createReminderInForUser(AUserInAServer user, String remindText, Duration remindIn, Long channelId) {
        return createReminderInForUser(user, remindText, remindIn, channelId, null);
    }

    private void scheduleReminder(Duration remindIn, Reminder reminder) {
        if(remindIn.getSeconds() < 60) {
            reminder.setJobTriggerKey(null);
            log.info("Directly scheduling unremind for reminder {}, because it was below the threshold.", reminder.getId());
            instantReminderScheduler.schedule(() -> {
                try {
                    self.executeReminder(reminder.getId());
                } catch (Exception exception) {
                    log.error("Failed to remind immediately.", exception);
                }
            }, remindIn.toNanos(), TimeUnit.NANOSECONDS);
        } else {
            HashMap<Object, Object> parameters = new HashMap<>();
            parameters.put("reminderId", reminder.getId().toString());
            JobParameters jobParameters = JobParameters
                    .builder()
                    .parameters(parameters)
                    .build();
            String triggerKey = schedulerService.executeJobWithParametersOnce("reminderJob", "utility", jobParameters, Date.from(reminder.getTargetDate()));
            log.info("Starting scheduled job  with trigger {} to execute reminder {}.", triggerKey, reminder.getId());
            reminder.setJobTriggerKey(triggerKey);
            reminderManagementService.saveReminder(reminder);
        }
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
        Long userId = reminderToRemindFor.getRemindedUser().getUserReference().getId();
        log.info("Executing reminder {} in channel {} in server {} for user {}.",
                reminderId, channel.getId(), server.getId(), userId);
        Optional<Guild> guildToAnswerIn = guildService.getGuildByIdOptional(server.getId());
        if(guildToAnswerIn.isPresent()) {
            Optional<GuildMessageChannel> channelToAnswerIn = channelService.getMessageChannelFromServerOptional(server.getId(), channel.getId());
            // only send the message if the channel still exists, if not, only set the reminder to reminded.
            if(channelToAnswerIn.isPresent()) {
                memberService.getMemberInServerAsync(server.getId(), userId).thenAccept(member ->
                    self.sendReminderText(reminderId, channelToAnswerIn.get(), member)
                ).exceptionally(throwable -> {
                    log.warn("Member {} not anymore in server {} - not reminding.", userId, server.getId(), throwable);
                    return null;
                });
            } else {
                log.warn("Channel {} in server {} to remind user did not exist anymore. Ignoring reminder {}", channel.getId(), server.getId(), reminderId);
            }
        } else {
            log.warn("Guild {} to remind user in did not exist anymore. Ignoring reminder {}.", server.getId(), reminderId);
        }
        reminderManagementService.setReminded(reminderToRemindFor);
    }

    @Transactional
    public CompletableFuture<Void> sendReminderText(Long reminderId, GuildMessageChannel channelToAnswerIn, Member member) {
        Reminder reminder = reminderManagementService.loadReminder(reminderId);
        log.debug("Sending remind message for reminder {} to user user {} in server {}.", reminderId, member.getIdLong(), member.getGuild().getIdLong());
        List<ReminderParticipant> participants = reminderParticipantManagementService.getReminderParticipants(reminder);
        List<MemberDisplay> participantsDisplays = participants
                .stream()
                .map(reminderParticipant -> MemberDisplay.fromAUserInAServer(reminderParticipant.getParticipant()))
                .collect(Collectors.toList());
        ExecutedReminderModel build = ExecutedReminderModel
                .builder()
                .reminder(reminder)
                .reminderParticipants(participantsDisplays)
                .memberNameDisplay(MemberNameDisplay.fromMember(member))
                .duration(Duration.between(reminder.getReminderDate(), reminder.getTargetDate()))
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(REMINDER_TEMPLATE_TEXT, build, channelToAnswerIn.getGuild().getIdLong());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, channelToAnswerIn));
    }

    @Override
    public void unRemind(Long reminderId, AUserInAServer aUserInAServer) {
        log.info("Trying to end reminder {} for user {} in server {}.", reminderId, aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId());
        Reminder reminder = reminderManagementService.getReminderByAndByUserNotReminded(aUserInAServer, reminderId).orElseThrow(() -> new ReminderNotFoundException(reminderId));
        reminder.setReminded(true);
        if(reminder.getJobTriggerKey() != null) {
            log.debug("Stopping scheduled trigger {} for reminder {}.", reminder.getJobTriggerKey(), reminderId);
            schedulerService.stopTrigger(reminder.getJobTriggerKey());
        }
    }

    @Override
    public void snoozeReminder(Long reminderId, AUserInAServer user, Duration newDuration) {
        Reminder reminder = reminderManagementService.getReminderByAndByUser(user, reminderId).orElseThrow(() -> new ReminderNotFoundException(reminderId));
        if(reminder.getTargetDate().isAfter(Instant.now()) && !reminder.isReminded()) {
            throw new NotPossibleToSnoozeException();
        }
        log.info("Snoozing reminder {} to be executed in {}.", reminderId, newDuration);
        reminder.setTargetDate(Instant.now().plus(newDuration));
        reminder.setReminded(false);
        scheduleReminder(newDuration, reminder);
    }
}

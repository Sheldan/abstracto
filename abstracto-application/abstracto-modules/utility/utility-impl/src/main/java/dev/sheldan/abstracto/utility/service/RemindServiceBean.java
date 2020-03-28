package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.management.ChannelManagementService;
import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.embed.MessageToSend;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.scheduling.model.SchedulerService;
import dev.sheldan.abstracto.templating.TemplateService;
import dev.sheldan.abstracto.utility.models.Reminder;
import dev.sheldan.abstracto.utility.models.template.ExecutedReminderModel;
import dev.sheldan.abstracto.utility.models.template.ReminderModel;
import dev.sheldan.abstracto.utility.service.management.ReminderManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
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
public class RemindServiceBean implements ReminderService {

    public static final String REMINDER_EMBED_KEY = "remind_response";
    @Autowired
    private ReminderManagementService reminderManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private Bot bot;

    @Autowired
    private ReminderService self;

    @Override
    public void createReminderInForUser(AUserInAServer user, String remindText, Duration remindIn, ReminderModel reminderModel) {
        AChannel channel = channelManagementService.loadChannel(reminderModel.getChannel().getId());
        AServerAChannelAUser aServerAChannelAUser = AServerAChannelAUser
                .builder()
                .user(user.getUserReference())
                .aUserInAServer(user)
                .guild(user.getServerReference())
                .channel(channel)
                .build();
        Instant remindAt = Instant.now().plusNanos(remindIn.toNanos());
        Reminder reminder = reminderManagementService.createReminder(aServerAChannelAUser, remindText, remindAt, reminderModel.getMessage().getIdLong());
        reminderModel.setReminder(reminder);
        MessageToSend message = templateService.renderEmbedTemplate(REMINDER_EMBED_KEY, reminderModel);
        reminderModel.getTextChannel().sendMessage(message.getMessage()).embed(message.getEmbed()).queue();

        if(remindIn.getSeconds() < 60) {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.schedule(() -> {
                self.executeReminder(reminder.getId());
            }, remindIn.toNanos(), TimeUnit.NANOSECONDS);
        } else {
            JobDataMap parameters = new JobDataMap();
            parameters.putAsString("reminderId", reminder.getId());
            schedulerService.executeJobWithParametersOnce("reminderJob", "utility", parameters, Date.from(reminder.getTargetDate()));
        }
    }

    @Override
    @Transactional
    public void executeReminder(Long reminderId) {
        Reminder reminderToRemindFor = reminderManagementService.loadReminder(reminderId);
        AServer server = reminderToRemindFor.getServer();
        AChannel channel = reminderToRemindFor.getChannel();
        AUser userReference = reminderToRemindFor.getToBeReminded().getUserReference();
        Member memberInServer = bot.getMemberInServer(server.getId(), userReference.getId());
        ExecutedReminderModel build = ExecutedReminderModel
                .builder()
                .reminder(reminderToRemindFor)
                .member(memberInServer)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate("remind_reminder", build);
        // todo, if channel does not exist anymore
        TextChannel channelToAnswerIn = bot.getTextChannelFromServer(server.getId(), channel.getId());
        channelToAnswerIn.sendMessage(messageToSend.getMessage()).embed(messageToSend.getEmbed()).queue();
    }
}

package dev.sheldan.abstracto.remind.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.remind.command.Remind;
import dev.sheldan.abstracto.remind.config.RemindFeatureDefinition;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import dev.sheldan.abstracto.remind.model.database.ReminderParticipant;
import dev.sheldan.abstracto.remind.model.template.listener.ReminderJoiningModel;
import dev.sheldan.abstracto.remind.payload.JoinReminderPayload;
import dev.sheldan.abstracto.remind.service.management.ReminderManagementService;
import dev.sheldan.abstracto.remind.service.management.ReminderParticipantManagementServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class ReminderJoinButtonListener implements ButtonClickedListener {

    public static final String REMIND_JOINING_NOTIFICATION_TEMPLATE = "remind_joining_notification";
    @Autowired
    private ReminderParticipantManagementServiceBean reminderParticipantManagementServiceBean;

    @Autowired
    private ReminderManagementService reminderManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        JoinReminderPayload payload = (JoinReminderPayload) model.getDeserializedPayload();
        ReminderJoiningModel joiningNotificationModel = new ReminderJoiningModel();
        if(payload.getRemindedUserId() == model.getEvent().getUser().getIdLong()) {
            joiningNotificationModel.setSelfJoin(true);
        } else {
            Reminder reminder = reminderManagementService.loadReminder(payload.getReminderId());
            AUserInAServer buttonClicker = userInServerManagementService.loadOrCreateUser(model.getEvent().getMember());
            Optional<ReminderParticipant> existingParticipant = reminderParticipantManagementServiceBean.getReminderParticipant(reminder, buttonClicker);
            boolean reminderIsInThePast = reminder.getTargetDate().isBefore(Instant.now());
            if(reminderIsInThePast) {
                joiningNotificationModel.setFailedToJoin(true);
            } else {
                if(existingParticipant.isPresent()) {
                    joiningNotificationModel.setJoined(false);
                    reminderParticipantManagementServiceBean.removeMemberFromReminder(existingParticipant.get());
                } else {
                    joiningNotificationModel.setReminderDate(reminder.getTargetDate());
                    joiningNotificationModel.setJoined(true);
                    reminderParticipantManagementServiceBean.addMemberToReminder(reminder, buttonClicker);
                }
            }
        }

        interactionService.sendMessageToInteraction(REMIND_JOINING_NOTIFICATION_TEMPLATE, joiningNotificationModel, model.getEvent().getHook());
        return ButtonClickedListenerResult.ACKNOWLEDGED;
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return model.getEvent().isFromGuild() && model.getOrigin().equals(Remind.REMINDER_JOIN_BUTTON_ORIGIN);
    }

    @Override
    public FeatureDefinition getFeature() {
        return RemindFeatureDefinition.REMIND;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }

}

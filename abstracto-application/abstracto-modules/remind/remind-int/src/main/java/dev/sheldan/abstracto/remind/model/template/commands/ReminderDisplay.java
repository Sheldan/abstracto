package dev.sheldan.abstracto.remind.model.template.commands;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class ReminderDisplay {
    private Long id;
    private Boolean reminded;
    private String text;
    private Instant targetDate;
    private Instant creationDate;
    private Boolean sentInDm;
    private Boolean userCommand;
    @Builder.Default
    private Boolean joined = false;
    private ServerChannelMessage message;

    public static ReminderDisplay fromReminder(Reminder reminder) {
        ServerChannelMessage message = ServerChannelMessage
                .builder()
                .messageId(reminder.getMessageId())
                .channelId(reminder.getChannel() != null ? reminder.getChannel().getId() : null)
                .serverId(reminder.getServer() != null ? reminder.getServer().getId() : null)
                .build();
        return ReminderDisplay
                .builder()
                .creationDate(reminder.getReminderDate())
                .targetDate(reminder.getTargetDate())
                .id(reminder.getId())
                .sentInDm(reminder.getSendInDm())
                .reminded(reminder.getReminded())
                .userCommand(reminder.getUserCommand())
                .message(message)
                .text(reminder.getText())
                .build();
    }
}

package dev.sheldan.abstracto.remind.model.template.commands;

import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.MemberNameDisplay;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.core.utils.MessageUtils;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
@SuperBuilder
public class ExecutedReminderModel extends ServerContext {
    private Long reminderId;
    private Long serverId;
    private Long channelId;
    private Long messageId;
    private UserDisplay userDisplay;
    private ReminderDisplay reminderDisplay;
    private List<MemberDisplay> reminderParticipants;
    private Duration duration;

    public String getMessageUrl() {
        return MessageUtils.buildMessageUrl(serverId , channelId, messageId);
    }
}

package dev.sheldan.abstracto.remind.model.template.commands;

import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.MemberNameDisplay;
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
    private Reminder reminder;
    private MemberNameDisplay memberNameDisplay;
    private List<MemberDisplay> reminderParticipants;
    private Duration duration;

    public String getMessageUrl() {
        return MessageUtils.buildMessageUrl(this.reminder.getServer().getId() ,this.reminder.getChannel().getId(), this.reminder.getMessageId());
    }
}

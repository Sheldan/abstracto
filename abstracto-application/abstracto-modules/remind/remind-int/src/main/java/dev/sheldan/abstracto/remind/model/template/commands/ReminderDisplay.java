package dev.sheldan.abstracto.remind.model.template.commands;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReminderDisplay {
    private Reminder reminder;
    private ServerChannelMessage message;
}

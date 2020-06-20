package dev.sheldan.abstracto.utility.models.template.commands.reminder;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.utility.models.database.Reminder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ReminderModel extends UserInitiatedServerContext {
    private String remindText;
    private Reminder reminder;
}

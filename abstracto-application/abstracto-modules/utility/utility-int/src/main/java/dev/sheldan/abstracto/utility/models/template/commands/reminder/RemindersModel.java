package dev.sheldan.abstracto.utility.models.template.commands.reminder;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.utility.models.database.Reminder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class RemindersModel extends UserInitiatedServerContext {
    private List<Reminder> reminders;
}

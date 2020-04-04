package dev.sheldan.abstracto.utility.models.template;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.utility.models.Reminder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Message;

@Getter
@Setter
@SuperBuilder
public class ReminderModel extends UserInitiatedServerContext {
    private String remindText;
    private Reminder reminder;
    private Message message;
}

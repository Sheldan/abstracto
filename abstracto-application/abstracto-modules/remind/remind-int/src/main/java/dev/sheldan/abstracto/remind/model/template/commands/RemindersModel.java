package dev.sheldan.abstracto.remind.model.template.commands;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
public class RemindersModel extends UserInitiatedServerContext {
    @Builder.Default
    private List<ReminderDisplay> reminders = new ArrayList<>();
}

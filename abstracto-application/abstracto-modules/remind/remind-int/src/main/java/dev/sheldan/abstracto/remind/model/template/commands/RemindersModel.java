package dev.sheldan.abstracto.remind.model.template.commands;

import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class RemindersModel {
    @Builder.Default
    private List<ReminderDisplay> reminders = new ArrayList<>();
    private UserDisplay userDisplay;
}

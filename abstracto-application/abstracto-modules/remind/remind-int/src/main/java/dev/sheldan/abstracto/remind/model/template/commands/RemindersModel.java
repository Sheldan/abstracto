package dev.sheldan.abstracto.remind.model.template.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class RemindersModel {
    @Builder.Default
    private List<ReminderDisplay> reminders = new ArrayList<>();
    private Member member;
}

package dev.sheldan.abstracto.remind.model.template.commands;

import dev.sheldan.abstracto.remind.model.database.Reminder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@Builder
public class ReminderModel {
    private String remindText;
    private Member member;
    private Reminder reminder;
}

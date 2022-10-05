package dev.sheldan.abstracto.remind.model.template.commands;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.template.display.MemberNameDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReminderModel {
    private String remindText;
    private MemberNameDisplay memberDisplay;
    private ReminderDisplay reminder;
    private ServerChannelMessage message;
    private String joinButtonId;

}

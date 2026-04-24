package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ModmailThreadReminderModel {
    private List<RoleDisplay> pingRoles;
    private MemberDisplay memberDisplay;
    private Instant autoCloseInstant;
    private boolean paused;
    private Instant created;
    private Instant updated;
}

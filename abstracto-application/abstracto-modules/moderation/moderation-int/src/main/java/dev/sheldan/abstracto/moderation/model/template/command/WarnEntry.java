package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.MemberDisplayModel;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * A single warning containing the full user instead of only the warning object when the warnings command is executed.
 * The template is: "warnings_warn_entry"
 */
@Getter
@Setter
@Builder
public class WarnEntry {
    private String reason;
    private Long warnId;
    private Long serverId;
    private Boolean decayed;
    private Instant warnDate;
    private Instant decayDate;
    /**
     * The {@link MemberDisplayModel} containing information about the user being warned. The member property is null if the user left the server
     */
    private MemberDisplay warnedUser;
    /**
     * The {@link MemberDisplayModel} containing information about the user warning. The member property is null if the user left the server
     */
    private MemberDisplay warningUser;
}

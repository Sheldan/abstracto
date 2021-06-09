package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.MemberDisplayModel;
import dev.sheldan.abstracto.moderation.model.database.Warning;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * A single warning containing the full user instead of only the warning object when the warnings command is executed.
 * The template is: "warnings_warn_entry"
 */
@Getter
@Setter
@Builder
public class WarnEntry {
    /**
     * The {@link Warning} of this entry
     */
    private Warning warning;
    /**
     * The {@link MemberDisplayModel} containing information about the user being warned. The member property is null if the user left the server
     */
    private MemberDisplayModel warnedUser;
    /**
     * The {@link MemberDisplayModel} containing information about the user warning. The member property is null if the user left the server
     */
    private MemberDisplayModel warningUser;
}

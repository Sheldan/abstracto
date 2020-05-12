package dev.sheldan.abstracto.moderation.models.template.job;

import dev.sheldan.abstracto.moderation.models.database.Warning;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

/**
 * A single warning containing the full user instead of only the warning object when logging the decayed warnings
 * The template is: "warnDecay_log_warn_entry_en_US.ftl"
 */
@Getter
@Setter
@Builder
public class WarnDecayWarning {
    /**
     * The persisted {@link Warning} object from the database containing the information about the warning
     */
    private Warning warning;
    /**
     * The member which was warned, is null if the user left the server
     */
    private Member warnedMember;
    /**
     * The user which casted the warn, is null if the user left the server
     */
    private Member warningMember;
}

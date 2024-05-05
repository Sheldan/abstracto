package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.time.Duration;
import java.time.Instant;

@Getter
@SuperBuilder
public class MuteLogModel {
    /**
     * The {@link Member} being muted
     */
    private MemberDisplay mutedMember;
    /**
     * The {@link Member} executing the mute
     */
    private MemberDisplay mutingMember;
    /**
     * The persisted mute object from the database containing the information about the mute
     */
    private Long muteId;
    private Instant muteTargetDate;
    private Instant oldMuteTargetDate;
    private Duration duration;
    private String reason;

    public boolean getMuteEnded() {
        return oldMuteTargetDate != null && muteTargetDate == null || oldMuteTargetDate == null && muteTargetDate == null;
    }

    public boolean getMuted() {
        return oldMuteTargetDate == null && muteTargetDate != null;
    }

    public boolean getDurationChanged() {
        return oldMuteTargetDate != null
                && muteTargetDate != null
                && !muteTargetDate.equals(oldMuteTargetDate);
    }
}

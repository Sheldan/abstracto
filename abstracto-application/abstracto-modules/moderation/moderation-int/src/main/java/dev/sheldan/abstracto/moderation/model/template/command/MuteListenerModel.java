package dev.sheldan.abstracto.moderation.model.template.command;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.time.Duration;
import java.time.Instant;

@Getter
@SuperBuilder
public class MuteListenerModel {
    /**
     * The {@link Member} being muted
     */
    private Member mutedUser;
    /**
     * The {@link Member} executing the mute
     */
    private Member mutingUser;
    /**
     * The persisted mute object from the database containing the information about the mute
     */
    private Long muteId;
    private Instant muteTargetDate;
    private Instant oldMuteTargetDate;
    private String reason;
    private Long channelId;

    /**
     * The {@link Duration} of the mute between the mute was cast and and the date it should end
     * @return The {@link Duration} between start and target date
     */
    public Duration getMuteDuration() {
        return Duration.between(Instant.now(), muteTargetDate);
    }

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

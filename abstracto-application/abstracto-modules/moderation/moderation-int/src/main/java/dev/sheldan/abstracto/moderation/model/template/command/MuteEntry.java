package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.MemberDisplayModel;
import dev.sheldan.abstracto.moderation.model.database.Mute;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
@Builder
public class MuteEntry {
    /**
     * The {@link Mute} of this entry
     */
    private Mute mute;
    /**
     * The {@link MemberDisplayModel} containing information about the user being muted. The member property is null if the user left the server
     */
    private MemberDisplayModel mutedUser;
    /**
     * The {@link MemberDisplayModel} containing information about the user muting. The member property is null if the user left the server
     */
    private MemberDisplayModel mutingUser;
    private Duration muteDuration;
}

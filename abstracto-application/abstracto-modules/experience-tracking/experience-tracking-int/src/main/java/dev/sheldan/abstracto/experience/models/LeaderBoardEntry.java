package dev.sheldan.abstracto.experience.models;

import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Object containing a {@link AUserExperience} object and the respective rank of the user in the guild.
 */
@Getter
@Setter
@Builder
public class LeaderBoardEntry {
    /**
     * Object representing the current experience status of a user in a guild.
     */
    private AUserExperience experience;
    /**
     * The rank this user has in the respective guild.
     */
    private Integer rank;
}

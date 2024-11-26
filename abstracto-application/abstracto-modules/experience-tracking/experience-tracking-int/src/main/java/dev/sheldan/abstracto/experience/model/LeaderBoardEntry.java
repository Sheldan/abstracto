package dev.sheldan.abstracto.experience.model;

import dev.sheldan.abstracto.experience.model.database.AUserExperience;
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
    private Long userId;
    private Integer level;
    private Long experience;
    private Long messageCount;
    private Integer rank;

    public static LeaderBoardEntry fromAUserExperience(AUserExperience aUserExperience) {
        return LeaderBoardEntry
            .builder()
            .experience(aUserExperience.getExperience())
            .userId(aUserExperience.getUser().getUserReference().getId())
            .messageCount(aUserExperience.getMessageCount())
            .level(aUserExperience.getLevelOrDefault())
            .build();
    }
}

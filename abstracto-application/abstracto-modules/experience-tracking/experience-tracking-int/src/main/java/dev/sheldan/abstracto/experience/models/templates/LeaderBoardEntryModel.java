package dev.sheldan.abstracto.experience.models.templates;

import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

/**
 * Model used in the list of members when rendering the leaderboard template. The reason this is necessary,
 * is because we need more than just the {@link AUserExperience} object, we also need the position of the user in this
 * guild and the {@link Member} for convenience in the templates.
 */
@Getter
@Setter
@Builder
public class LeaderBoardEntryModel {
    private AUserExperience experience;
    private Member member;
    private Integer rank;
}

package dev.sheldan.abstracto.experience.models.templates;

import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

import java.io.Serializable;

/**
 * Model used in the list of members when rendering the leader board template. The reason this is necessary,
 * is because we need more than just the {@link AUserExperience} object, we also need the position of the user in this
 * guild and the {@link Member} for convenience in the templates.
 */
@Getter
@Builder
public class LeaderBoardEntryModel implements Serializable {
    private AUserExperience experience;
    private transient Member member;
    private Integer rank;
}

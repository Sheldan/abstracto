package dev.sheldan.abstracto.experience.model.template;

import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
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
    private Long experience;
    private Long messageCount;
    private Long userId;
    private Integer level;
    /**
     * The {@link Member member} associated wit this user experience, might be null if the user left he server.
     */
    @Setter
    private transient Member member;
    /**
     * The position this {@link dev.sheldan.abstracto.core.models.database.AUserInAServer user} in this server has, ordered by experience {@link AUserExperience} experience
     */
    private Integer rank;
}

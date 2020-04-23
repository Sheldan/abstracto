package dev.sheldan.abstracto.experience.models.templates;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Object containing the provided property to render the rank command template. This includes the
 * {@link LeaderBoardEntryModel} object containing the information from the user executing the command, as well as the
 * experience needed until next level.
 */
@Getter
@Setter
@SuperBuilder
public class RankModel extends UserInitiatedServerContext {
    /**
     * The {@link LeaderBoardEntryModel} containing the experience information about the user executing the rank
     * command.
     */
    private LeaderBoardEntryModel rankUser;
    /**
     * The necessary experience until the next level up.
     */
    private Long experienceToNextLevel;
}

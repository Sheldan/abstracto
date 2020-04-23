package dev.sheldan.abstracto.experience.models.templates;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Object containing the complete information passed to the leaderboard template. It contains the leaderboard
 * information of the requested page of the total users and the leaderboard information of the user executing the
 * command.
 */
@Getter
@Setter
@SuperBuilder
public class LeaderBoardModel extends UserInitiatedServerContext {
    /**
     * List of {@link LeaderBoardEntryModel} containing the information about the users from the requested page.
     */
    private List<LeaderBoardEntryModel> userExperiences;
    /**
     * The {@link LeaderBoardEntryModel} containing the leaderboard information executing the command.
     */
    private LeaderBoardEntryModel userExecuting;
}

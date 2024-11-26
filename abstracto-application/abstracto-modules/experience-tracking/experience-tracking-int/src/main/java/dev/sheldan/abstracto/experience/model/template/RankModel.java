package dev.sheldan.abstracto.experience.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

/**
 * Object containing the provided property to render the rank command template. This includes the
 * {@link LeaderBoardEntryModel} object containing the information from the user executing the command, as well as the
 * experience needed until next level.
 */
@Getter
@Setter
@Builder
public class RankModel {
    /**
     * The {@link LeaderBoardEntryModel} containing the experience information about the user executing the rank
     * command.
     */
    private LeaderBoardEntryModel rankUser;
    /**
     * The necessary experience to the next level up.
     */
    private Long experienceToNextLevel;
    /**
     * Total experience needed for this level
     */
    private Long experienceForCurrentLevel;
    /**
     * Percentage of progress within this level
     */
    private Float currentLevelPercentage;
    /**
     * The total amount of experience needed for this level
     */
    private Long levelExperience;
    /**
     * The experience which has been reached _within_ this level
     */
    private Long inLevelExperience;
    /**
     * The experience needed to reach the next level
     */
    private Long nextLevelExperience;
    /**
     * The member to show the rank for
     */
    private Member member;
    private String leaderboardUrl;
}

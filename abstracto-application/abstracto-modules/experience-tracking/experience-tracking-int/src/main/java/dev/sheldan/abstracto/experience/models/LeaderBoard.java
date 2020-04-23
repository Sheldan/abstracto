package dev.sheldan.abstracto.experience.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Wrapper object containing a list of {@link LeaderBoardEntry} representing a leaderboard.
 */
@Getter
@Setter
@Builder
public class LeaderBoard {
    /**
     * List of {@link LeaderBoardEntry} representing the leaderboard.
     */
    private List<LeaderBoardEntry> entries;
}

package dev.sheldan.abstracto.entertainment.model.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreditsLeaderboardResponseModel {
    private List<CreditsLeaderboardEntry> entries;
    private CreditsLeaderboardEntry ownRank;
}

package dev.sheldan.abstracto.entertainment.model.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreditsModel {
    private CreditsLeaderboardEntry entry;
}

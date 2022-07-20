package dev.sheldan.abstracto.entertainment.model.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayDayResponseModel {
    private Long currentCredits;
    private Long leaderboardPosition;
    private Long gainedCredits;
}

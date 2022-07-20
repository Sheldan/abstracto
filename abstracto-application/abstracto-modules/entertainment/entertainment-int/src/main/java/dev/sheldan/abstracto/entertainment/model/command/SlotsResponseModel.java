package dev.sheldan.abstracto.entertainment.model.command;

import dev.sheldan.abstracto.entertainment.dto.SlotsResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class SlotsResponseModel {
    private Long bid;
    private Long oldCredits;
    private Long newCredits;
    private Long winnings;
    private Long factor;
    private String outComeKey;
    private List<List<String>> rows;

    public static SlotsResponseModel fromSlotsResult(SlotsResult result) {
        return SlotsResponseModel
                .builder()
                .bid(result.getBid())
                .factor(result.getFactor())
                .newCredits(result.getNewCredits())
                .outComeKey(result.getOutComeKey())
                .oldCredits(result.getOldCredits())
                .rows(result.getRows())
                .winnings(result.getWinnings())
                .build();
    }
}

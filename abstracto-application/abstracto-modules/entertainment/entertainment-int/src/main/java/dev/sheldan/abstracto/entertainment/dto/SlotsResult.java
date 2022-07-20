package dev.sheldan.abstracto.entertainment.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class SlotsResult {
    private Long bid;
    private Long oldCredits;
    private Long newCredits;
    private Long winnings;
    private Long factor;
    private String outComeKey;
    private List<List<String>> rows;
}

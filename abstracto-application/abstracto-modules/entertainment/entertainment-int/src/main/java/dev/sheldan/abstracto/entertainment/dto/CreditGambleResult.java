package dev.sheldan.abstracto.entertainment.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class CreditGambleResult {
    private List<Integer> rolls;
    private Integer uniqueNumbers;
    private Long bid;
    private Long toBank;
    private Long toJackpot;
    private Long currentJackpot;
    private Boolean won;
}

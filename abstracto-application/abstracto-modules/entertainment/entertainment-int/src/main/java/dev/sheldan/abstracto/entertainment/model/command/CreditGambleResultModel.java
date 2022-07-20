package dev.sheldan.abstracto.entertainment.model.command;

import dev.sheldan.abstracto.entertainment.dto.CreditGambleResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreditGambleResultModel {
    private List<Integer> rolls;
    private Integer uniqueNumbers;
    private Long bid;
    private Long toBank;
    private Long toJackpot;
    private Long currentJackpot;
    private Boolean won;

    public static CreditGambleResultModel fromCreditGambleResult(CreditGambleResult creditGambleResult) {
        return CreditGambleResultModel
                .builder()
                .rolls(creditGambleResult.getRolls())
                .uniqueNumbers(creditGambleResult.getUniqueNumbers())
                .bid(creditGambleResult.getBid())
                .toBank(creditGambleResult.getToBank())
                .toJackpot(creditGambleResult.getToJackpot())
                .currentJackpot(creditGambleResult.getCurrentJackpot())
                .won(creditGambleResult.getWon())
                .build();
    }
}

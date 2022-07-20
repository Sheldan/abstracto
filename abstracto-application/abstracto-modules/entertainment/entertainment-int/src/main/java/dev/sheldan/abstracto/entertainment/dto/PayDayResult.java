package dev.sheldan.abstracto.entertainment.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PayDayResult {
    private Long currentCredits;
    private Long gainedCredits;
}

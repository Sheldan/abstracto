package dev.sheldan.abstracto.core.command.condition;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ConditionResult {
    private boolean result;
    private String reason;
    private ConditionDetail conditionDetail;

    public static ConditionResult fromSuccess() {
        return ConditionResult.builder().result(true).build();
    }

    public static ConditionResult fromFailure(ConditionDetail detail) {
        return ConditionResult.builder().result(false).conditionDetail(detail).build();
    }
}

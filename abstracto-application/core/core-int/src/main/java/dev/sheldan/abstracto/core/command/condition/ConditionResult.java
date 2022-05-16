package dev.sheldan.abstracto.core.command.condition;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Builder
public class ConditionResult {
    private boolean result;
    private String reason;
    private ConditionDetail conditionDetail;
    @Builder.Default
    private boolean reportResult = true;

    public static final ConditionResult SUCCESS = ConditionResult.builder().result(true).build();

    public static ConditionResult fromSuccess() {
        return ConditionResult.builder().result(true).build();
    }

    public static CompletableFuture<ConditionResult> fromAsyncSuccess() {
        return CompletableFuture.completedFuture(fromSuccess());
    }

    public static ConditionResult fromFailure(ConditionDetail detail) {
        return ConditionResult.builder().result(false).conditionDetail(detail).build();
    }
}

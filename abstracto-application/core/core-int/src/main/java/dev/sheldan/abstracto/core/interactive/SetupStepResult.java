package dev.sheldan.abstracto.core.interactive;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SetupStepResult {
    private List<DelayedActionConfigContainer> delayedActionConfigList;
    private SetupStepResultType result;

    public static SetupStepResult fromCancelled() {
        return SetupStepResult.builder().result(SetupStepResultType.CANCELLED).build();
    }

    public static SetupStepResult fromSuccess() {
        return SetupStepResult.builder().result(SetupStepResultType.SUCCESS).build();
    }
}

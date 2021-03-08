package dev.sheldan.abstracto.core.interactive;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SetupStepResult {
    private List<DelayedActionConfig> delayedActionConfigList;
    private SetupStepResultType result;

    public static SetupStepResult fromCancelled() {
        return SetupStepResult.builder().result(SetupStepResultType.CANCELLED).build();
    }
}

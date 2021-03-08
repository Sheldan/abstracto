package dev.sheldan.abstracto.core.interactive;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SetupExecution {
    private SetupStep step;
    private SetupStepParameter parameter;
    private SetupExecution nextStep;
}

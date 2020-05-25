package dev.sheldan.abstracto.core.interactive;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmptySetupParameter implements SetupStepParameter {
    private Long previousMessageId;
}

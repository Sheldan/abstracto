package dev.sheldan.abstracto.core.interactive;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SetupSummaryStepParameter implements SetupStepParameter {
    private Long previousMessageId;
    private List<DelayedActionConfig> delayedActionList;
}

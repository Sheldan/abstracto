package dev.sheldan.abstracto.core.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ConditionResult {
    private boolean result;
    private String reason;
}

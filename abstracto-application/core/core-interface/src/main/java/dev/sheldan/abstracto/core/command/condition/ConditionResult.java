package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ConditionResult {
    private boolean result;
    private String reason;
    private Object additionalInfo;
    private AbstractoRunTimeException exception;
}

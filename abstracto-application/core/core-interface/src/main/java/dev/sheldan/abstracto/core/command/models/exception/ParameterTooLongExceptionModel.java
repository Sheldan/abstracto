package dev.sheldan.abstracto.core.command.models.exception;

import dev.sheldan.abstracto.core.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class ParameterTooLongExceptionModel implements Serializable {
    private final transient Command command;
    private final String parameterName;
    private final Integer actualLength;
    private final Integer maximumLength;
}

package dev.sheldan.abstracto.core.command.model.exception;

import dev.sheldan.abstracto.core.command.Command;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class IncorrectParameterExceptionModel implements Serializable {
    private final transient Command command;
    private final String parameterName;
}

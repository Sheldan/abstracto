package dev.sheldan.abstracto.core.command.model.exception;

import dev.sheldan.abstracto.core.command.Command;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class InsufficientParametersExceptionModel implements Serializable {
    private final transient Command command;
    private final String parameterName;
}

package dev.sheldan.abstracto.core.command.model.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class CommandAliasHidesCommandModel implements Serializable {
    private final String existingCommand;
}

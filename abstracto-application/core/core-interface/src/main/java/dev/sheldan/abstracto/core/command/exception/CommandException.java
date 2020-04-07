package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;

public class CommandException extends AbstractoRunTimeException {
    public CommandException(String message) {
        super(message);
    }
}

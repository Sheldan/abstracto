package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;

public class ValidatorConfigException extends AbstractoRunTimeException {
    public ValidatorConfigException(String message) {
        super(message);
    }
}

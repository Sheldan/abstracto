package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;

public class IncorrectParameterTypeException extends AbstractoRunTimeException {

    public IncorrectParameterTypeException(Command command, Class clazz, String parameterName) {
        super(String.format("Incorrect parameter given for parameter. Expected %s for parameter %s in command %s.", clazz.getName(), parameterName, command.getConfiguration().getName()));
    }
}

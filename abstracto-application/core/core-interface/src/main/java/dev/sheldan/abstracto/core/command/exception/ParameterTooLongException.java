package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.models.exception.ParameterTooLongExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.templating.Templatable;

public class ParameterTooLongException extends AbstractoRunTimeException implements Templatable {

    private final ParameterTooLongExceptionModel model;

    public ParameterTooLongException(Command command, String parameterName, Integer actualLength, Integer maximumLength) {
        super("Parameter was too long for command");
        this.model = ParameterTooLongExceptionModel
                .builder()
                .actualLength(actualLength)
                .maximumLength(maximumLength)
                .parameterName(parameterName)
                .command(command)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "parameter_too_long_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}

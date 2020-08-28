package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.models.exception.IncorrectParameterExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.templating.Templatable;

public class IncorrectParameterException extends AbstractoRunTimeException implements Templatable {

    private final IncorrectParameterExceptionModel model;

    public IncorrectParameterException(Command command, Class expected, String parameterName) {
        super("Incorrect parameter given for parameter");
        this.model = IncorrectParameterExceptionModel.builder().clazz(expected).parameterName(parameterName).command(command).build();
    }

    @Override
    public String getTemplateName() {
        return "incorrect_parameters_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}

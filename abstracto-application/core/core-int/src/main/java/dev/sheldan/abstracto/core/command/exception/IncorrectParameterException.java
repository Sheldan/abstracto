package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.model.exception.IncorrectParameterExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class IncorrectParameterException extends AbstractoRunTimeException implements Templatable {

    private final IncorrectParameterExceptionModel model;

    public IncorrectParameterException(Command command, String parameterName) {
        super("Incorrect parameter given for parameter");
        this.model = IncorrectParameterExceptionModel
                .builder()
                .parameterName(parameterName)
                .command(command)
                .build();
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

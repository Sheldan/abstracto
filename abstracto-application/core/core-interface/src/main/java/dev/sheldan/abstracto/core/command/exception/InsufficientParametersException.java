package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.models.exception.InsufficientParametersExceptionModel;
import dev.sheldan.abstracto.templating.Templatable;
import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;

public class InsufficientParametersException extends AbstractoRunTimeException implements Templatable {

    private final InsufficientParametersExceptionModel model;

    public InsufficientParametersException(Command command, String parameterName) {
        super("Insufficient parameters given for command");
        this.model = InsufficientParametersExceptionModel.builder().command(command).parameterName(parameterName).build();
    }

    @Override
    public String getTemplateName() {
        return "insufficient_parameters_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}

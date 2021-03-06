package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.model.exception.InsufficientParametersExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

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

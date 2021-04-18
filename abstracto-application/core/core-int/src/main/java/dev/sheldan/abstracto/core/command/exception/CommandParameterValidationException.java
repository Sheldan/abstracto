package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.validator.ValidatorParam;
import dev.sheldan.abstracto.core.command.model.exception.CommandParameterValidationExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CommandParameterValidationException extends AbstractoRunTimeException implements Templatable {

    private final CommandParameterValidationExceptionModel model;

    public CommandParameterValidationException(List<ValidatorParam> validatorParams, String template, Parameter parameter) {
        super("Command parameter failed to validate.");
        this.model = CommandParameterValidationExceptionModel
                .builder()
                .validationTemplate(template)
                .validatorParams(validatorParams)
                .parameter(parameter)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "command_parameter_validation_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}

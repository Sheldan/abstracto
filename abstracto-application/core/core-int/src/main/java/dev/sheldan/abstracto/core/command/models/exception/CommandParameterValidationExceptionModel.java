package dev.sheldan.abstracto.core.command.models.exception;

import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.validator.ValidatorParam;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class CommandParameterValidationExceptionModel implements Serializable {
    private List<ValidatorParam> validatorParams;
    private String validationTemplate;
    private Parameter parameter;
}

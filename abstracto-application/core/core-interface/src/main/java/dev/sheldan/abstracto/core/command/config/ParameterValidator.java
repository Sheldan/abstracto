package dev.sheldan.abstracto.core.command.config;

import dev.sheldan.abstracto.core.command.config.validator.ValidatorParam;

import java.util.List;

public interface ParameterValidator {
    boolean validate(Object value);
    List<ValidatorParam> getParameters();
    String getTemplateName();
}

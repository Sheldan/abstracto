package dev.sheldan.abstracto.core.command.config;

import dev.sheldan.abstracto.core.command.config.validator.ValidatorParam;
import dev.sheldan.abstracto.core.templating.Templatable;

import java.util.List;

public interface ParameterValidator extends Templatable {
    boolean validate(Object value);
    List<ValidatorParam> getParameters();
    String getExceptionTemplateName();
}

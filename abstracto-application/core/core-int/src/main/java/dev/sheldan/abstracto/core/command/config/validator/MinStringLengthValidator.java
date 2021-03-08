package dev.sheldan.abstracto.core.command.config.validator;

import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.exception.ValidatorConfigException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Builder
public class MinStringLengthValidator implements ParameterValidator {

    private Long minLength;

    @Override
    public boolean validate(Object value) {
        if(value == null) {
            throw new IllegalArgumentException("Object to validate must not be null");
        }
        if(!(value instanceof String)) {
            throw new ValidatorConfigException("Incorrect value passed to min string length validator.");
        }
        SingleNumberValidatorParam param = (SingleNumberValidatorParam) getParameters().get(0);
        String stringValue = (String) value;
        return stringValue.length() >= param.getNumber();
    }

    @Override
    public List<ValidatorParam> getParameters() {
        SingleNumberValidatorParam param = SingleNumberValidatorParam
                .builder()
                .number(minLength)
                .build();
        return Arrays.asList(param);
    }

    @Override
    public String getExceptionTemplateName() {
        return "command_parameter_validation_string_too_short";
    }

    @Override
    public String getTemplateName() {
        return "min_string_length_validation_description";
    }

    @Override
    public Object getTemplateModel() {
        return getParameters().get(0);
    }
}

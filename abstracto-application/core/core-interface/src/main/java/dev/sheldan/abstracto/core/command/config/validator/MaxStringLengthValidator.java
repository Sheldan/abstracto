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
public class MaxStringLengthValidator implements ParameterValidator {

    private Long maxLength;

    @Override
    public boolean validate(Object value) {
        if(value == null) {
            throw new IllegalArgumentException("Object to validate must not be null");
        }
        if(!(value instanceof String)) {
            throw new ValidatorConfigException("Incorrect value passed to max string length validator.");
        }
        SingleNumberValidatorParam param = (SingleNumberValidatorParam) getParameters().get(0);
        String stringValue = (String) value;
        return stringValue.length() <= param.getNumber();
    }

    @Override
    public List<ValidatorParam> getParameters() {
        SingleNumberValidatorParam param = SingleNumberValidatorParam
                .builder()
                .number(maxLength)
                .build();
        return Arrays.asList(param);
    }

    @Override
    public String getTemplateName() {
        return "command_parameter_validation_string_too_long";
    }

    public static MaxStringLengthValidator max(Long number) {
        return  MaxStringLengthValidator
                .builder()
                .maxLength(number)
                .build();
    }


}

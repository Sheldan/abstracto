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
public class MinIntegerValueValidator implements ParameterValidator {

    private Long minValue;

    @Override
    public boolean validate(Object value) {
        if(value == null) {
            throw new IllegalArgumentException("Object to validate must not be null");
        }
        boolean isLong = value instanceof Long;
        boolean isInteger = value instanceof Integer;
        if(!isLong && !isInteger) {
            throw new ValidatorConfigException("Incorrect value passed to max value validator.");
        }
        SingleNumberValidatorParam param = (SingleNumberValidatorParam) getParameters().get(0);
        if(isLong) {
            Long longValue = (Long) value;
            return longValue >= param.getNumber();
        } else {
            Integer integerValue = (Integer) value;
            return integerValue >= param.getNumber();
        }
    }

    @Override
    public List<ValidatorParam> getParameters() {
        SingleNumberValidatorParam param = SingleNumberValidatorParam
                .builder()
                .number(minValue)
                .build();
        return Arrays.asList(param);
    }

    public static MinIntegerValueValidator min(Long number) {
        return  MinIntegerValueValidator
                .builder()
                .minValue(number)
                .build();
    }

    @Override
    public String getExceptionTemplateName() {
        return "command_parameter_validation_value_too_small";
    }


    @Override
    public String getTemplateName() {
        return "min_integer_value_validation_description";
    }

    @Override
    public Object getTemplateModel() {
        return getParameters().get(0);
    }
}

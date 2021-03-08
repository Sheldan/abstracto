package dev.sheldan.abstracto.core.command.config.validator;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SingleNumberValidatorParam implements ValidatorParam {
    private Long number;
}

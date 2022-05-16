package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class EffectConfig {
    private Integer position;
    private String effectKey;
    private String parameterName;
}

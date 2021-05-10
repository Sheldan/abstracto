package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EffectConfig {
    private Integer position;
    private String effectKey;
}

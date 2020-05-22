package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.models.database.ADefaultConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SetupSystemConfigMessageModel {
    private String configKey;
    private ADefaultConfig defaultConfig;
}

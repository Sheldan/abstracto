package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SetupInitialMessageModel {
    private FeatureConfig featureConfig;
}

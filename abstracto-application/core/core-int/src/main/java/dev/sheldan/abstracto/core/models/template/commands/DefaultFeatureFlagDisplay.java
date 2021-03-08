package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.property.FeatureFlagProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DefaultFeatureFlagDisplay {
    private FeatureFlagProperty featureFlagProperty;
    private FeatureConfig featureConfig;
}

package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FeatureFlagDisplay {
    private AFeatureFlag featureFlag;
    private FeatureConfig featureConfig;
}

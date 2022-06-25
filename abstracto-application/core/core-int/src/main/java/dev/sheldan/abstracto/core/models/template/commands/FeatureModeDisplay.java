package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.database.AFeatureMode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FeatureModeDisplay {
    private AFeatureModeDisplay featureMode;
    private FeatureConfig featureConfig;
    private Boolean isDefaultValue;
}

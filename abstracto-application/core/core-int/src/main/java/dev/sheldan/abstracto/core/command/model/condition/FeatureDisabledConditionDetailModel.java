package dev.sheldan.abstracto.core.command.model.condition;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Builder
public class FeatureDisabledConditionDetailModel implements Serializable {
    private FeatureConfig featureConfig;
}

package dev.sheldan.abstracto.core.command.models.exception;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class FeatureDisabledMessage {
    private FeatureConfig featureConfig;
}

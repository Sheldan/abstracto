package dev.sheldan.abstracto.core.command.models;

import dev.sheldan.abstracto.core.config.FeatureDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class FeatureDisabledMessage {
    private FeatureDisplay featureDisplay;
}

package dev.sheldan.abstracto.core.command.models.exception;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Builder
public class FeatureDisabledExceptionModel implements Serializable {
    private FeatureConfig featureConfig;
}

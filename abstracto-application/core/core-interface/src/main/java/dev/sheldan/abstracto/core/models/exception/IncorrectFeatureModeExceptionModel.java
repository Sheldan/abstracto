package dev.sheldan.abstracto.core.models.exception;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class IncorrectFeatureModeExceptionModel implements Serializable {
    private List<FeatureMode> requiredModes;
    private Command command;
    private FeatureEnum featureEnum;
}

package dev.sheldan.abstracto.core.command.model.exception;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class IncorrectFeatureModeMessage {
    private FeatureConfig featureConfig;
    private Command command;
}

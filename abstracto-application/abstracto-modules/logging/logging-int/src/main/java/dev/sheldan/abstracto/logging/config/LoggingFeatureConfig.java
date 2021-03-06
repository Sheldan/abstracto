package dev.sheldan.abstracto.logging.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class LoggingFeatureConfig implements FeatureConfig {

    @Override
    public FeatureDefinition getFeature() {
        return LoggingFeatureDefinition.LOGGING;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(LoggingPostTarget.DELETE_LOG, LoggingPostTarget.EDIT_LOG,  LoggingPostTarget.JOIN_LOG, LoggingPostTarget.LEAVE_LOG);
    }

}

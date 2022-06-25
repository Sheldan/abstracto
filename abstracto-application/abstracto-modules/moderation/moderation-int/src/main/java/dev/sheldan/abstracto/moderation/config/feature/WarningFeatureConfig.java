package dev.sheldan.abstracto.moderation.config.feature;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.moderation.config.feature.mode.WarningMode;
import dev.sheldan.abstracto.moderation.config.posttarget.WarningPostTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class WarningFeatureConfig implements FeatureConfig {

    public static final String WARN_INFRACTION_POINTS = "warnInfractionPoints";

    @Autowired
    private WarningDecayFeatureConfig warningDecayFeatureConfig;

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.WARNING;
    }

    @Override
    public List<FeatureConfig> getDependantFeatures() {
        return Arrays.asList(warningDecayFeatureConfig);
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(WarningPostTarget.WARN_LOG);
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(WarningMode.WARN_DECAY_LOG);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(WARN_INFRACTION_POINTS);
    }
}

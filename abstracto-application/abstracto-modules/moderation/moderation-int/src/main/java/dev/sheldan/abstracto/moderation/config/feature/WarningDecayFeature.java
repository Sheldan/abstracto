package dev.sheldan.abstracto.moderation.config.feature;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.moderation.config.feature.mode.WarnDecayMode;
import dev.sheldan.abstracto.moderation.config.posttarget.WarnDecayPostTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class WarningDecayFeature implements FeatureConfig {

    public static final String DECAY_DAYS_KEY = "decayDays";
    @Autowired
    private WarningFeature warningFeature;


    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.AUTOMATIC_WARN_DECAY;
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(DECAY_DAYS_KEY);
    }

    @Override
    public List<FeatureConfig> getRequiredFeatures() {
        return Arrays.asList(warningFeature);
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(WarnDecayPostTarget.DECAY_LOG);
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(WarnDecayMode.AUTOMATIC_WARN_DECAY_LOG);
    }
}

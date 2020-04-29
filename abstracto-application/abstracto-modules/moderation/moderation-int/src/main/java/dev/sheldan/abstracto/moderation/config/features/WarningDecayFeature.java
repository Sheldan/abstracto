package dev.sheldan.abstracto.moderation.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class WarningDecayFeature implements FeatureConfig {

    @Autowired
    private WarningFeature warningFeature;

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.AUTOMATIC_WARN_DECAY;
    }

    @Override
    public List<FeatureConfig> getRequiredFeatures() {
        return Arrays.asList(warningFeature);
    }
}

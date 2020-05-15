package dev.sheldan.abstracto.moderation.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.service.FeatureValidator;
import dev.sheldan.abstracto.moderation.config.posttargets.WarnDecayPostTarget;
import dev.sheldan.abstracto.moderation.validator.WarnDecayFeatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class WarningDecayFeature implements FeatureConfig {

    @Autowired
    private WarningFeature warningFeature;

    @Autowired
    private WarnDecayFeatureValidator warnDecayFeatureValidator;

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.AUTOMATIC_WARN_DECAY;
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
    public List<FeatureValidator> getAdditionalFeatureValidators() {
        return Arrays.asList(warnDecayFeatureValidator);
    }
}

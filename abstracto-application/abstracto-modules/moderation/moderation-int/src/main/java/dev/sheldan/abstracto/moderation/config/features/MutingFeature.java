package dev.sheldan.abstracto.moderation.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.moderation.config.features.mode.MutingMode;
import dev.sheldan.abstracto.moderation.config.posttargets.MutingPostTarget;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MutingFeature implements FeatureConfig {

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.MUTING;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(MutingPostTarget.MUTE_LOG);
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(MutingMode.MANUAL_UN_MUTE_LOGGING, MutingMode.MUTE_LOGGING, MutingMode.UN_MUTE_LOGGING);
    }
}

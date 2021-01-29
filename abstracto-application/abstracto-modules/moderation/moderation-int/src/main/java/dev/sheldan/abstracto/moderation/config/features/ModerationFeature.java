package dev.sheldan.abstracto.moderation.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.moderation.config.features.mode.ModerationMode;
import dev.sheldan.abstracto.moderation.config.posttargets.ModerationPostTarget;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ModerationFeature implements FeatureConfig {

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.MODERATION;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(ModerationPostTarget.BAN_LOG, ModerationPostTarget.KICK_LOG);
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(ModerationMode.BAN_LOG, ModerationMode.KICK_LOG);
    }
}

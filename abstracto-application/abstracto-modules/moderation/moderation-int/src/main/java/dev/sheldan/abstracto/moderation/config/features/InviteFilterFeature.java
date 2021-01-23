package dev.sheldan.abstracto.moderation.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.moderation.config.posttargets.InviteFilterPostTarget;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class InviteFilterFeature implements FeatureConfig {
    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.INVITE_FILTER;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(InviteFilterPostTarget.INVITE_DELETE_LOG);
    }

}

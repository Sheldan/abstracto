package dev.sheldan.abstracto.moderation.config.feature;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.moderation.config.posttarget.InviteFilterPostTarget;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class InviteFilterFeature implements FeatureConfig {
    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.INVITE_FILTER;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(InviteFilterPostTarget.INVITE_DELETE_LOG);
    }

}

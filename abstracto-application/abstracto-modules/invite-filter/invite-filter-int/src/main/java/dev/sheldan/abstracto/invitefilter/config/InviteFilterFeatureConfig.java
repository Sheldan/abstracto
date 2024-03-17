package dev.sheldan.abstracto.invitefilter.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class InviteFilterFeatureConfig implements FeatureConfig {
    @Override
    public FeatureDefinition getFeature() {
        return InviteFilterFeatureDefinition.INVITE_FILTER;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(InviteFilterPostTarget.INVITE_DELETE_LOG);
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(InviteFilterMode.values());
    }
}

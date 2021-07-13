package dev.sheldan.abstracto.antiraid.config;

import dev.sheldan.abstracto.antiraid.service.MassPingService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.moderation.config.feature.MutingFeatureConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AntiRaidFeatureConfig implements FeatureConfig {

    @Autowired
    private MutingFeatureConfig mutingFeatureConfig;

    @Override
    public FeatureDefinition getFeature() {
        return AntiRaidFeatureDefinition.ANTI_RAID;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(AntiRaidPostTarget.MASS_PING_LOG);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(MassPingService.MAX_AFFECTED_LEVEL_KEY);
    }

    @Override
    public List<FeatureConfig> getRequiredFeatures() {
        return Arrays.asList(mutingFeatureConfig);
    }
}

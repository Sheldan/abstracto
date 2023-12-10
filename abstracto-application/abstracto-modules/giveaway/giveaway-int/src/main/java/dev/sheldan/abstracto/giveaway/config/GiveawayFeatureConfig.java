package dev.sheldan.abstracto.giveaway.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class GiveawayFeatureConfig implements FeatureConfig {

    @Override
    public FeatureDefinition getFeature() {
        return GiveawayFeatureDefinition.GIVEAWAY;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(GiveawayPostTarget.GIVEAWAYS);
    }
}

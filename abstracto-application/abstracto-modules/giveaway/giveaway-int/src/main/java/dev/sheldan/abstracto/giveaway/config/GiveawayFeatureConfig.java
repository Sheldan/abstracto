package dev.sheldan.abstracto.giveaway.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class GiveawayFeatureConfig implements FeatureConfig {

    public static final String KEY_GIVEAWAYS_DURATION = "keyGiveawaysDuration";

    @Override
    public FeatureDefinition getFeature() {
        return GiveawayFeatureDefinition.GIVEAWAY;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(GiveawayPostTarget.GIVEAWAYS);
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(GiveawayMode.KEY_GIVEAWAYS, GiveawayMode.AUTO_NOTIFY_GIVEAWAY_KEY_WINNERS);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(KEY_GIVEAWAYS_DURATION);
    }
}

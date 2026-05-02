package dev.sheldan.abstracto.twitch.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class TwitchFeatureConfig implements FeatureConfig {

    public static final String TWITCH_REFRESH_INTERVAL = "twitchRefreshInterval";

    @Override
    public FeatureDefinition getFeature() {
        return TwitchFeatureDefinition.TWITCH;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(TwitchPostTarget.TWITCH_LIVE_NOTIFICATION);
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(TwitchFeatureMode.DELETE_NOTIFICATION, TwitchFeatureMode.UPDATE_NOTIFICATION);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return List.of(TWITCH_REFRESH_INTERVAL);
    }
}

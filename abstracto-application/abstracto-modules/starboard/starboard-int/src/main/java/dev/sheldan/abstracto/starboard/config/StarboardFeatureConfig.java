package dev.sheldan.abstracto.starboard.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class StarboardFeatureConfig implements FeatureConfig {

    public static final String STAR_LVL_CONFIG_PREFIX = "starLvl";
    public static final String STAR_EMOTE_PREFIX = "star";
    public static final String STAR_BADGE_EMOTE_PREFIX = "starboardBadge";
    public static final String STAR_LEVELS_CONFIG_KEY = "starLvls";
    public static final String STAR_EMOTE = "star";

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Override
    public FeatureDefinition getFeature() {
        return StarboardFeatureDefinition.STARBOARD;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(StarboardPostTarget.STARBOARD);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        List<String> configKeys = new ArrayList<>();
        int maxLevels = getMaxLevels();
        for(int i = maxLevels; i > 0; i--) {
            configKeys.add(StarboardFeatureConfig.STAR_LVL_CONFIG_PREFIX + i);
        }
        return configKeys;
    }

    @Override
    public List<String> getRequiredEmotes() {
        List<String> emoteNames = new ArrayList<>();
        int maxLevels = getMaxLevels();
        for(int i = maxLevels; i > 0; i--) {
            emoteNames.add(StarboardFeatureConfig.STAR_EMOTE_PREFIX + i);
        }
        emoteNames.add(StarboardFeatureConfig.STAR_BADGE_EMOTE_PREFIX + 1);
        emoteNames.add(StarboardFeatureConfig.STAR_BADGE_EMOTE_PREFIX + 2);
        emoteNames.add(StarboardFeatureConfig.STAR_BADGE_EMOTE_PREFIX + 3);
        return emoteNames;
    }

    private int getMaxLevels() {
        return defaultConfigManagementService.getDefaultConfig(STAR_LEVELS_CONFIG_KEY).getLongValue().intValue();
    }
}

package dev.sheldan.abstracto.utility.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.service.FeatureValidator;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.utility.StarboardFeatureValidator;
import dev.sheldan.abstracto.utility.config.posttargets.StarboardPostTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class StarboardFeature implements FeatureConfig {

    public static final String STAR_LVL_CONFIG_PREFIX = "starLvl";
    public static final String STAR_EMOTE_PREFIX = "star";
    public static final String STAR_BADGE_EMOTE_PREFIX = "starboardBadge";
    public static final String STAR_LEVELS_CONFIG_KEY = "starLvls";

    @Autowired
    private StarboardFeatureValidator starboardFeatureValidator;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.STARBOARD;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(StarboardPostTarget.STARBOARD);
    }

    @Override
    public List<FeatureValidator> getAdditionalFeatureValidators() {
        return Arrays.asList(starboardFeatureValidator);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        List<String> configKeys = new ArrayList<>();
        int maxLevels = getMaxLevels();
        for(int i = maxLevels; i > 0; i--) {
            configKeys.add(StarboardFeature.STAR_LVL_CONFIG_PREFIX + i);
        }
        return configKeys;
    }

    @Override
    public List<String> getRequiredEmotes() {
        List<String> emoteNames = new ArrayList<>();
        int maxLevels = getMaxLevels();
        for(int i = maxLevels; i > 0; i--) {
            emoteNames.add(StarboardFeature.STAR_EMOTE_PREFIX + i);
        }
        emoteNames.add(StarboardFeature.STAR_BADGE_EMOTE_PREFIX + 1);
        emoteNames.add(StarboardFeature.STAR_BADGE_EMOTE_PREFIX + 2);
        emoteNames.add(StarboardFeature.STAR_BADGE_EMOTE_PREFIX + 3);
        return emoteNames;
    }

    private int getMaxLevels() {
        return defaultConfigManagementService.getDefaultConfig(STAR_LEVELS_CONFIG_KEY).getLongValue().intValue();
    }
}

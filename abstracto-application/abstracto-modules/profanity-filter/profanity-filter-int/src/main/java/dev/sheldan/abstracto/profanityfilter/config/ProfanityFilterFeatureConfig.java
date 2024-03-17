package dev.sheldan.abstracto.profanityfilter.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.profanityfilter.service.ProfanityFilterService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ProfanityFilterFeatureConfig implements FeatureConfig {
    @Override
    public FeatureDefinition getFeature() {
        return ProfanityFilterFeatureDefinition.PROFANITY_FILTER;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(ProfanityFilterPostTarget.PROFANITY_FILTER_QUEUE);
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(ProfanityFilterMode.values());
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(ProfanityFilterService.PROFANITY_VOTES_CONFIG_KEY);
    }

    @Override
    public List<String> getRequiredEmotes() {
        return Arrays.asList(ProfanityFilterService.REPORT_AGREE_EMOTE, ProfanityFilterService.REPORT_DISAGREE_EMOTE);
    }


}

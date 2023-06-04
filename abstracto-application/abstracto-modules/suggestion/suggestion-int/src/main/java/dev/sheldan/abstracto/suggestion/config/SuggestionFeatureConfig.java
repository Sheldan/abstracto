package dev.sheldan.abstracto.suggestion.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.suggestion.service.SuggestionService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SuggestionFeatureConfig implements FeatureConfig {

    public static final String SUGGESTION_AGREE_EMOTE = "suggestionYes";
    public static final String SUGGESTION_DISAGREE_EMOTE = "suggestionNo";

    @Override
    public FeatureDefinition getFeature() {
        return SuggestionFeatureDefinition.SUGGEST;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(SuggestionPostTarget.SUGGESTION, SuggestionPostTarget.SUGGESTION_REMINDER);
    }

    @Override
    public List<String> getRequiredEmotes() {
        return Arrays.asList(SUGGESTION_AGREE_EMOTE, SUGGESTION_DISAGREE_EMOTE);
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(
                SuggestionFeatureMode.SUGGESTION_REMINDER,
                SuggestionFeatureMode.SUGGESTION_BUTTONS,
                SuggestionFeatureMode.SUGGESTION_AUTO_EVALUATE,
                SuggestionFeatureMode.SUGGESTION_THREAD
        );
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(
                SuggestionService.SUGGESTION_REMINDER_DAYS_CONFIG_KEY,
                SuggestionService.SUGGESTION_AUTO_EVALUATE_DAYS_CONFIG_KEY,
                SuggestionService.SUGGESTION_AUTO_EVALUATE_PERCENTAGE_CONFIG_KEY
        );
    }
}

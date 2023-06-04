package dev.sheldan.abstracto.suggestion.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.suggestion.service.PollService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PollFeatureConfig implements FeatureConfig {
    @Override
    public FeatureDefinition getFeature() {
        return SuggestionFeatureDefinition.POLL;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(PollPostTarget.POLLS, PollPostTarget.POLL_REMINDER);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(PollService.SERVER_POLL_DURATION_SECONDS);
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(PollFeatureMode.POLL_AUTO_EVALUATE, PollFeatureMode.POLL_REMINDER);
    }
}

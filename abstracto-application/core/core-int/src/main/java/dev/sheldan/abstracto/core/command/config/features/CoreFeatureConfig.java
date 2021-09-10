package dev.sheldan.abstracto.core.command.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CoreFeatureConfig implements FeatureConfig {

    public static final String NO_COMMAND_REPORTING_CONFIG_KEY = "noCommandFoundReporting";
    public static final String SUCCESS_REACTION_KEY = "successReaction";
    public static final String WARN_REACTION_KEY = "warnReaction";
    public static final String MAX_MESSAGES_KEY = "maxMessages";
    public static final String CONFIRMATION_TIMEOUT = "confirmationTimeout";

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }

    @Override
    public List<String> getRequiredEmotes() {
        return Arrays.asList(WARN_REACTION_KEY, SUCCESS_REACTION_KEY);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(NO_COMMAND_REPORTING_CONFIG_KEY, MAX_MESSAGES_KEY, CONFIRMATION_TIMEOUT);
    }
}

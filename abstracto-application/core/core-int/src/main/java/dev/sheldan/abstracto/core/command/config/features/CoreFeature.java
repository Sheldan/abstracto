package dev.sheldan.abstracto.core.command.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CoreFeature implements FeatureConfig {

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }

    @Override
    public List<String> getRequiredEmotes() {
        return Arrays.asList("warnReaction", "successReaction");
    }
}

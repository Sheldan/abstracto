package dev.sheldan.abstracto.core.command.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CoreFeature implements FeatureConfig {

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }

    @Override
    public List<String> getRequiredEmotes() {
        return Arrays.asList("warnReaction", "successReaction");
    }
}

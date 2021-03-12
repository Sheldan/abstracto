package dev.sheldan.abstracto.linkembed.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class LinkEmbedFeature implements FeatureConfig {

    @Override
    public FeatureDefinition getFeature() {
        return LinkEmbedFeatureDefinition.LINK_EMBEDS;
    }

    @Override
    public List<String> getRequiredEmotes() {
        return Arrays.asList("removeEmbed");
    }
}

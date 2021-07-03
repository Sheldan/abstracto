package dev.sheldan.abstracto.linkembed.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class LinkEmbedFeatureConfig implements FeatureConfig {

    @Override
    public FeatureDefinition getFeature() {
        return LinkEmbedFeatureDefinition.LINK_EMBEDS;
    }

    @Override
    public List<String> getRequiredEmotes() {
        return Arrays.asList("removeEmbed");
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(LinkEmbedFeatureMode.DELETE_BUTTON);
    }
}

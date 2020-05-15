package dev.sheldan.abstracto.utility.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class LinkEmbedFeature implements FeatureConfig {

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.LINK_EMBEDS;
    }

    @Override
    public List<String> getRequiredEmotes() {
        return Arrays.asList("removeEmbed");
    }
}

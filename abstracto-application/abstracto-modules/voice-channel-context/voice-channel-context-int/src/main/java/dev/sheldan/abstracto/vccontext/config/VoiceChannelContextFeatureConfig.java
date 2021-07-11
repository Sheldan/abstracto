package dev.sheldan.abstracto.vccontext.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

@Component
public class VoiceChannelContextFeatureConfig implements FeatureConfig {

    @Override
    public FeatureDefinition getFeature() {
        return VoiceChannelContextFeatureDefinition.VOICE_CHANNEL_CONTEXT;
    }

}

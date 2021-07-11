package dev.sheldan.abstracto.vccontext.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum VoiceChannelContextFeatureDefinition implements FeatureDefinition {
    VOICE_CHANNEL_CONTEXT("voiceChannelContext");

    private String key;

    VoiceChannelContextFeatureDefinition(String key) {
        this.key = key;
    }
}

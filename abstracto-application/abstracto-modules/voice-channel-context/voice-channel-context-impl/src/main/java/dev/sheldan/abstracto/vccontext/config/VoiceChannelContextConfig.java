package dev.sheldan.abstracto.vccontext.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:voice-channel-context-config.properties")
public class VoiceChannelContextConfig {
}


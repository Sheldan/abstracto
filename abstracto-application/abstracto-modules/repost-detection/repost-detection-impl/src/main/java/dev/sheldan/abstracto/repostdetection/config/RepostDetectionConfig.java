package dev.sheldan.abstracto.repostdetection.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:repost-detection-config.properties")
public class RepostDetectionConfig {
}


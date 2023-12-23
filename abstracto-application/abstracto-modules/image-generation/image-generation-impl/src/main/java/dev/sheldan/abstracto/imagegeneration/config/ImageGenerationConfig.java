package dev.sheldan.abstracto.imagegeneration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:image-generation-config.properties")
public class ImageGenerationConfig {
}


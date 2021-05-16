package dev.sheldan.abstracto.profanityfilter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:profanityFilter-config.properties")
public class ProfanityFilterProperties {
}

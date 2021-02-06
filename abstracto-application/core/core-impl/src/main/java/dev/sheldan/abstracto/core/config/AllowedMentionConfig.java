package dev.sheldan.abstracto.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "abstracto.allowedmention")
public class AllowedMentionConfig {
    private Boolean everyone;
    private Boolean role;
    private Boolean user;
}

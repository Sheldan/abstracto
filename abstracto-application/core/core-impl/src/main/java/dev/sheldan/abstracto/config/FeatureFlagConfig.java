package dev.sheldan.abstracto.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "abstracto")
public class FeatureFlagConfig {
    private HashMap<String, Boolean> features = new HashMap<>();
}

package dev.sheldan.abstracto.core.config;

import dev.sheldan.abstracto.core.models.config.FeaturePropertiesConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;


@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "abstracto")
public class FeatureConfigLoader {
    private HashMap<String, FeaturePropertiesConfig> features = new HashMap<>();
}




package dev.sheldan.abstracto.core.config;

import dev.sheldan.abstracto.core.models.property.FeatureFlagProperty;
import dev.sheldan.abstracto.core.models.property.FeatureModeProperty;
import dev.sheldan.abstracto.core.models.property.PostTargetProperty;
import dev.sheldan.abstracto.core.models.property.SystemConfigProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "abstracto")
public class DefaultConfigProperties {
    private Map<String, SystemConfigProperty> systemConfigs;
    private Map<String, FeatureFlagProperty> featureFlags;
    private Map<String, PostTargetProperty> postTargets;
    private Map<String, FeatureModeProperty> featureModes;
}

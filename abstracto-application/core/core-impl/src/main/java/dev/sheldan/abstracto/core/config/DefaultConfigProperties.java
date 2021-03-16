package dev.sheldan.abstracto.core.config;

import dev.sheldan.abstracto.core.models.property.FeatureFlagProperty;
import dev.sheldan.abstracto.core.models.property.FeatureModeProperty;
import dev.sheldan.abstracto.core.models.property.PostTargetProperty;
import dev.sheldan.abstracto.core.models.property.SystemConfigProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.*;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "abstracto")
public class DefaultConfigProperties {
    private Map<String, SystemConfigProperty> systemConfigs;
    private Map<String, FeatureFlagProperty> featureFlags;
    private Map<String, PostTargetProperty> postTargets;
    private Map<String, FeatureModeProperty> featureModes;

    /**
     * This is required to make the keys all lower case, so we can search for them faster, and also make it possible
     * for users to not require exact names
     */
    @PostConstruct
    public void postConstruct() {
       makeKeysLowerCase(systemConfigs);
       makeKeysLowerCase(featureFlags);
       makeKeysLowerCase(postTargets);
       makeKeysLowerCase(featureModes);
    }

    private <T> void makeKeysLowerCase(Map<String, T> map) {
        if(map == null) {
            return;
        }
        Set<String> keys = new HashSet<>(map.keySet());
        List<Pair<String, T>> pairs = new ArrayList<>();
        keys.forEach(s ->
            pairs.add(Pair.of(s.toLowerCase(), map.get(s)))
        );
        keys.forEach(map::remove);
        pairs.forEach(stringTPair -> map.put(stringTPair.getKey(), stringTPair.getValue()));
    }
}

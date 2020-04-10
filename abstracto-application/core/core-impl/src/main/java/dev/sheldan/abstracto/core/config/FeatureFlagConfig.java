package dev.sheldan.abstracto.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "abstracto")
public class FeatureFlagConfig {
    private HashMap<String, Boolean> features = new HashMap<>();

    public boolean doesFeatureExist(String name) {
        return features.containsKey(name);
    }

    public List<String> getFeaturesAsList() {
        return new ArrayList<>(features.keySet());
    }
}

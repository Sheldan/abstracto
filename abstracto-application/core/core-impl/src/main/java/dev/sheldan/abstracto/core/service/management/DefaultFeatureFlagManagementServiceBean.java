package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.DefaultConfigProperties;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.property.FeatureFlagProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultFeatureFlagManagementServiceBean implements  DefaultFeatureFlagManagementService{

    @Autowired
    private DefaultConfigProperties defaultConfigProperties;

    @Override
    public List<String> getDefaultFeatureKeys() {
        return new ArrayList<>(defaultConfigProperties.getFeatureFlags().keySet());
    }

    @Override
    public FeatureFlagProperty getDefaultFeatureFlagProperty(AFeature feature) {
        return findFeatureFlagPropertyViaFeatureKey(feature.getKey());
    }

    @Override
    public FeatureFlagProperty getDefaultFeatureFlagProperty(FeatureEnum feature) {
        return findFeatureFlagPropertyViaFeatureKey(feature.getKey());
    }

    @Override
    public List<FeatureFlagProperty> getAllDefaultFeatureFlags() {
        return new ArrayList<>(defaultConfigProperties.getFeatureFlags().values());
    }

    private FeatureFlagProperty findFeatureFlagPropertyViaFeatureKey(String featureKey) {
        return getAllDefaultFeatureFlags()
                .stream()
                .filter(featureFlagProperty -> featureFlagProperty.getFeatureName().equals(featureKey)).findFirst()
                .orElseThrow(() -> new AbstractoRunTimeException(String.format("Feature flag for feature %s has no default value.", featureKey)));
    }
}

package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.DefaultConfigProperties;
import dev.sheldan.abstracto.core.exception.FeatureModeNotFoundException;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.property.FeatureModeProperty;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DefaultFeatureModeManagementBean implements DefaultFeatureModeManagement {

    @Autowired
    private DefaultConfigProperties defaultConfigProperties;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Override
    public List<FeatureModeProperty> getFeatureModesForFeature(AFeature feature) {
        return defaultConfigProperties
                .getFeatureModes()
                .values()
                .stream()
                .filter(featureModeProperty -> featureModeProperty.getFeatureName().equals(feature.getKey()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FeatureModeProperty> getAll() {
        return new ArrayList<>(defaultConfigProperties.getFeatureModes().values());
    }

    @Override
    public Optional<FeatureModeProperty> getFeatureModeOptional(AFeature feature, String mode) {
        return Optional.ofNullable(defaultConfigProperties
                .getFeatureModes()
                .get(mode));
    }

    @Override
    public FeatureModeProperty getFeatureMode(AFeature feature, String mode) {
        return getFeatureModeOptional(feature, mode).orElseThrow(() -> new FeatureModeNotFoundException(mode, featureConfigService.getFeatureModesFromFeatureAsString(feature.getKey())));
    }
}

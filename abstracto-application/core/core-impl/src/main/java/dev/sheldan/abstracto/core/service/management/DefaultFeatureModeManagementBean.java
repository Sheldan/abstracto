package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.FeatureModeNotFoundException;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.DefaultFeatureMode;
import dev.sheldan.abstracto.core.repository.DefaultFeatureModeRepository;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DefaultFeatureModeManagementBean implements DefaultFeatureModeManagement {

    @Autowired
    private DefaultFeatureModeRepository defaultFeatureModeRepository;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Override
    public List<DefaultFeatureMode> getFeatureModesForFeature(AFeature feature) {
        return defaultFeatureModeRepository.findByFeature(feature);
    }

    @Override
    public List<DefaultFeatureMode> getAll() {
        return defaultFeatureModeRepository.findAll();
    }

    @Override
    public Optional<DefaultFeatureMode> getFeatureModeOptional(AFeature feature, String mode) {
        return defaultFeatureModeRepository.findByFeatureAndMode(feature, mode);
    }

    @Override
    public DefaultFeatureMode getFeatureMode(AFeature feature, String mode) {
        return getFeatureModeOptional(feature, mode).orElseThrow(() -> new FeatureModeNotFoundException(mode, featureConfigService.getFeatureModesFromFeatureAsString(feature.getKey())));
    }
}

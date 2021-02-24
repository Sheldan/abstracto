package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.repository.FeatureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FeatureManagementServiceBean implements FeatureManagementService {

    @Autowired
    private FeatureRepository featureRepository;

    @Override
    public AFeature createFeature(String key) {
        AFeature feature = AFeature
                .builder()
                .key(key)
                .build();
        featureRepository.save(feature);
        log.info("Creating new feature {}.", key);
        return feature;
    }

    @Override
    public boolean featureExists(String key) {
        return getFeature(key) != null;
    }

    @Override
    public AFeature getFeature(String key) {
        return featureRepository.findByKeyIgnoreCase(key);
    }
}

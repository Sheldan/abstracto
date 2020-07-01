package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.DefaultFeatureFlag;
import dev.sheldan.abstracto.core.repository.DefaultFeatureFlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultFeatureFlagManagementServiceBean implements  DefaultFeatureFlagManagementService{

    @Autowired
    private DefaultFeatureFlagRepository repository;

    @Override
    public List<String> getDefaultFeatureKeys() {
        return repository.findAll().stream().map(defaultFeatureFlag -> defaultFeatureFlag.getFeature().getKey()).collect(Collectors.toList());
    }

    @Override
    public List<DefaultFeatureFlag> getAllDefaultFeatureFlags() {
        return repository.findAll();
    }
}

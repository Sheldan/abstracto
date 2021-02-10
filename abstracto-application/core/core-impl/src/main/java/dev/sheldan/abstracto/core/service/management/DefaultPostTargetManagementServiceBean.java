package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.DefaultConfigProperties;
import dev.sheldan.abstracto.core.models.property.PostTargetProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultPostTargetManagementServiceBean implements DefaultPostTargetManagementService {

    @Autowired
    private DefaultConfigProperties defaultConfigProperties;

    @Override
    public List<PostTargetProperty> getAllDefaultPostTargets() {
        return new ArrayList<>(defaultConfigProperties.getPostTargets().values());
    }

    @Override
    public List<String> getDefaultPostTargetKeys() {
        return getAllDefaultPostTargets().stream().map(PostTargetProperty::getName).collect(Collectors.toList());
    }
}

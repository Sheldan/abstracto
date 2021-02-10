package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.property.PostTargetProperty;

import java.util.List;

public interface DefaultPostTargetManagementService {
    List<PostTargetProperty> getAllDefaultPostTargets();
    List<String> getDefaultPostTargetKeys();
}

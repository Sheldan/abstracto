package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.DefaultPostTarget;

import java.util.List;

public interface DefaultPostTargetManagementService {
    List<DefaultPostTarget> getAllDefaultPostTargets();
    List<String> getDefaultPostTargetKeys();
}

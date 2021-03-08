package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.Optional;

public interface RoleManagementService {
    ARole createRole(Long id, AServer server);
    Optional<ARole> findRoleOptional(Long id);
    ARole findRole(Long id);
    void markDeleted(ARole role);
}

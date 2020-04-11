package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;

public interface RoleManagementService {
    ARole createRole(Long id, AServer server);
    ARole findRole(Long id);
    void markDeleted(ARole role);
}

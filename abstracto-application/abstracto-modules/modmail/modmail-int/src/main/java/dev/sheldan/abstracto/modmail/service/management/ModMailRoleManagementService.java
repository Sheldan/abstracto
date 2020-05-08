package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailRole;

import java.util.List;

public interface ModMailRoleManagementService {
    void addRoleToModMailRoles(ARole role, AServer server);
    void removeRoleFromModMailRoles(ARole role, AServer server);
    List<ModMailRole> getRolesForServer(AServer server);
    boolean isRoleAlreadyAssigned(ARole role, AServer server);
}

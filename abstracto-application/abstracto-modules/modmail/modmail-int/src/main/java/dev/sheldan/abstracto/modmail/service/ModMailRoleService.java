package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;

public interface ModMailRoleService {
    void addRoleToModMailRoles(ARole role, AServer server);
    void removeRoleFromModMailRoles(ARole role, AServer server);
}

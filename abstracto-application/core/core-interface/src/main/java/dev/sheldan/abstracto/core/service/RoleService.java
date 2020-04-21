package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Role;

public interface RoleService {
    void addRoleToUser(AUserInAServer aUserInAServer, ARole role);
    void removeRoleFromUser(AUserInAServer aUserInAServer, ARole role);
    void markDeleted(Role role);
    void markDeleted(Long id);
    boolean isRoleInServer(AServer server, ARole role);
}

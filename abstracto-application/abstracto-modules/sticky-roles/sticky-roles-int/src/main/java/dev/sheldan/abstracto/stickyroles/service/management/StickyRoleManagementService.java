package dev.sheldan.abstracto.stickyroles.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.stickyroles.model.database.StickyRole;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public interface StickyRoleManagementService {
    default StickyRole getOrCreateStickyRole(Role role) {
        return getOrCreateStickyRole(role.getIdLong());
    }
    StickyRole getOrCreateStickyRole(Long roleId);

    StickyRole createStickyRole(Long roleId);
    List<StickyRole> createStickyRoles(List<Long> roleIds);
    List<StickyRole> getRoles(List<Long> roleIds);

    List<StickyRole> getStickyRolesForServer(AServer server);

    Boolean DEFAULT_STICKINESS = true;
}

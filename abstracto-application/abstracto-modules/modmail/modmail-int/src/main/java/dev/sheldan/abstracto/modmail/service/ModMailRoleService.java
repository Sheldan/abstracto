package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.core.models.database.ARole;

/**
 * Service for managing {@link dev.sheldan.abstracto.modmail.models.database.ModMailRole}, this includes crating and removing them
 */
public interface ModMailRoleService {
    /**
     * Adds a given {@link ARole} to the list of {@link dev.sheldan.abstracto.modmail.models.database.ModMailRole} of the given server.
     * This method also allows the given role to execute the mod mail related commands. (Which causes the commands to automatically be restricted)
     *  @param role The {@link ARole} to change to a {@link dev.sheldan.abstracto.modmail.models.database.ModMailRole}
     *
     */
    void addRoleToModMailRoles(ARole role);

    /**
     * Removes the given {@link ARole} from the list of {@link dev.sheldan.abstracto.modmail.models.database.ModMailRole} of the server.
     * This automatically dis-allows the mod mail related commands for the given role.
     * @param role The {@link ARole} to remove from the list of mod mail roles
     *
     */
    void removeRoleFromModMailRoles(ARole role);
}

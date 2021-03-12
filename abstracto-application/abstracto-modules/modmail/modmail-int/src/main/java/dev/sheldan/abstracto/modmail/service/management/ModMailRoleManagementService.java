package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.modmail.model.database.ModMailRole;

import java.util.List;

/**
 * Management service bean responsible to create, remove, retrieve and check mod mail roles which are used to determine
 * which roles are getting pinged when a new mod mail thread is created.
 */
public interface ModMailRoleManagementService {
    /**
     * Adds the given {@link ARole} to the mod mail roles of the {@link AServer}. This method does not check if the role
     * is already present.
     * @param role The {@link ARole} to add to the mod mail roles
     */
    void addRoleToModMailRoles(ARole role);

    /**
     * Removes the given {@link ARole} from the mod mail roles of the given {@link AServer}. Does nothing if the
     * role is not used as a mod mail role on the server
     * @param role The {@link ARole} to remove from the mod mail roles
     */
    void removeRoleFromModMailRoles(ARole role);

    /**
     * Retrieves all roles which should be pinged when a new mod mail thread is created by a user and returns the list
     * of {@link ModMailRole} for the given {@link AServer}.
     * @param server The {@link AServer} for which to retrieve the mod mail roles for
     * @return The list of found {@link ModMailRole} for the given {@link AServer}
     */
    List<ModMailRole> getRolesForServer(AServer server);

    /**
     * Checks whether or not the given {@link ARole} has already been assigned as a {@link ModMailRole} in the given {@link AServer}
     * @param role The {@link ARole} to check for
     * @return Whether or not the given {@link ARole} is used as a {@link ModMailRole} in the given {@link AServer}
     */
    boolean isRoleAlreadyAssigned(ARole role);
}

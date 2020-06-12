package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.ADisabledExpRole;

import java.util.List;

/**
 * Service used to manage the instances of {@link ADisabledExpRole} in the database, which includes creating, removing and retrieving them
 */
public interface DisabledExpRoleManagementService {

    /**
     * Creates an instance of {@link ADisabledExpRole} which marks the {@link ARole} as disabled. This effectively means, that the experience is disabled for members
     * which have the {@link ARole}
     * @param role The {@link ARole} which should be used as a role to disable experience
     * @return The create instance of {@link ADisabledExpRole}
     */
    ADisabledExpRole setRoleToBeDisabledForExp(ARole role);

    /**
     * Removes the given {@link ARole} from the list of roles which had their experience gain disabled. This method removes the instance from the list of
     * {@link ADisabledExpRole} and enables experience for the given {@link ARole}
     * @param role The {@link ARole} to enable experience for
     */
    void removeRoleToBeDisabledForExp(ARole role);

    /**
     * Retrieves all the {@link ADisabledExpRole} roles for a given {@link AServer}, which means, it returns all roles for which there is
     * @param server The {@link AServer} to retrieve all {@link ADisabledExpRole} for
     * @return A List of {@link ADisabledExpRole} which are currently on the {@link AServer}
     */
    List<ADisabledExpRole> getDisabledRolesForServer(AServer server);

    /**
     * Checks if the given {@link ARole} has its experience disabled and returns true if so
     * @param role The {@link ARole} to check for
     * @return Whether or not the given {@link ARole} has the experience disabled.
     */
    boolean isExperienceDisabledForRole(ARole role);
}

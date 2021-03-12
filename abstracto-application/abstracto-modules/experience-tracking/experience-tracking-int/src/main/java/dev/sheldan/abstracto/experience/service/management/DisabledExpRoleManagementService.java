package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.model.database.ADisabledExpRole;

import java.util.List;

/**
 * Service used to manage the instances of {@link ADisabledExpRole} in the database, which includes creating, removing and retrieving them
 */
public interface DisabledExpRoleManagementService {

    /**
     * Creates an instance of {@link ADisabledExpRole experienceRole} which marks the {@link ARole role} as disabled. This effectively means, that the experience is disabled for members
     * which have the {@link ARole role}
     * @param role The {@link ARole role} which should be used as a role to disable experience
     * @return The created instance of {@link ADisabledExpRole experienceRole}
     */
    ADisabledExpRole setRoleToBeDisabledForExp(ARole role);

    /**
     * Removes the given {@link ARole role} from the list of roles which had their experience gain disabled. This method removes the instance from the list of
     * {@link ADisabledExpRole roles} and enables experience for the given {@link ARole role}
     * @param role The {@link ARole role} to enable experience for
     */
    void removeRoleToBeDisabledForExp(ARole role);

    /**
     * Retrieves all the {@link ADisabledExpRole experienceRole} roles for a given {@link AServer server}
     * @param server The {@link AServer server} to retrieve all {@link ADisabledExpRole roles} for
     * @return A List of {@link ADisabledExpRole roles} which are currently configured for the {@link AServer server}
     */
    List<ADisabledExpRole> getDisabledRolesForServer(AServer server);

    /**
     * Checks if the given {@link ARole role} has its experience disabled and returns true if so
     * @param role The {@link ARole role} to check for
     * @return Whether or not the given {@link ARole role} has the experience disabled.
     */
    boolean isExperienceDisabledForRole(ARole role);
}

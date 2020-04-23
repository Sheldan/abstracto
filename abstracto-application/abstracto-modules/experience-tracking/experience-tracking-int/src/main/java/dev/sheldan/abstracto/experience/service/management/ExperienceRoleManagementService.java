package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;

import java.util.List;

/**
 * Service responsible to manage the {@link AExperienceRole} configuration of a server. This contains functionality to
 * set/unset a level to a certain role, retrieve {@link AExperienceRole} of a certain role and load all for a given
 * server.
 */
public interface ExperienceRoleManagementService {
    /**
     * Sets the given {@link AExperienceLevel} to the given {@link ARole} in the {@link AServer}. This will create an
     * {@link AExperienceRole} instance and store it. If the role was already set in the server, this sets this role to
     * the new level.
     * @param level The {@link AExperienceLevel} to set the role for
     * @param role The {@link ARole} to set to
     * @param server The {@link AServer} in which this should happen.
     */
    void setLevelToRole(AExperienceLevel level, ARole role, AServer server);

    /**
     * Deletes *all* (if there are multiple by some chance) roles which were set to be given at the provided {@link AExperienceLevel} in the {@link AServer}
     * @param level The level to remove the roles for
     * @param server The server in which this should happen
     */
    void removeAllRoleAssignmentsForLevelInServer(AExperienceLevel level, AServer server);

    /**
     * Deletes a singular {@link AExperienceRole} directly.
     * @param role The {@link AExperienceRole} to delete.
     */
    void unsetRole(AExperienceRole role);

    /**
     * Retrieves the {@link AExperienceRole} which uses the given {@link ARole} in the {@link AServer}
     * @param role The {@link ARole} to search for
     * @param server The {@link AServer} in which to search in
     * @return
     */
    AExperienceRole getRoleInServer(ARole role, AServer server);

    /**
     * Retrives all {@link AExperienceRole} configured in the given {@link AServer}
     * @param server The server to retrieve the list of {@link AExperienceRole} for
     * @return A list of {@link AExperienceRole} which are currently configured for the {@link AServer}
     */
    List<AExperienceRole> getExperienceRolesForServer(AServer server);
}

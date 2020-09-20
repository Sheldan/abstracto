package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;

import java.util.List;
import java.util.Optional;

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
     * @return the created or updated {@link AExperienceRole}
     */
    AExperienceRole setLevelToRole(AExperienceLevel level, ARole role);

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
     * @return the {@link AExperienceRole} which uses the given {@link ARole}
     */
    AExperienceRole getRoleInServer(ARole role);
    Optional<AExperienceRole> getRoleInServerOptional(ARole role);
    AExperienceRole getRoleInServer(Long roleId);

    /**
     * Retrieves all {@link AExperienceRole} configured in the given {@link AServer}
     * @param server The server to retrieve the list of {@link AExperienceRole} for
     * @return A list of {@link AExperienceRole} which are currently configured for the {@link AServer}
     */
    List<AExperienceRole> getExperienceRolesForServer(AServer server);

    AExperienceRole getExperienceRoleById(Long experienceRoleId);
    Optional<AExperienceRole> getExperienceRoleByIdOptional(Long experienceRoleId);
}

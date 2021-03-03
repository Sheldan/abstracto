package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;

import java.util.List;
import java.util.Optional;

/**
 * Service responsible to manage the {@link AExperienceRole experienceRole} configuration of a server. This contains functionality to
 * set/unset a level to a certain role, retrieve a {@link AExperienceRole experienceRole} of a certain role and load all for a given
 * server.
 */
public interface ExperienceRoleManagementService {
    /**
     * Sets the given {@link AExperienceLevel level} to the given {@link ARole role} in the {@link AServer server}. This will create an
     * {@link AExperienceRole experienceRole} instance and store it. If the role was already set in the server, this sets this role to
     * the new level.
     * @param level The {@link AExperienceLevel experienceRole} to set the role for
     * @param role The {@link ARole role} to set
     * @return the created or updated {@link AExperienceRole experienceRole}
     */
    AExperienceRole setLevelToRole(AExperienceLevel level, ARole role);

    /**
     * Deletes *all* (if there are multiple by some chance) roles which were set to be given at the provided {@link AExperienceLevel level} in the {@link AServer server}
     * @param level The level to remove the roles for
     * @param server The server in which this should happen
     */
    void removeAllRoleAssignmentsForLevelInServer(AExperienceLevel level, AServer server);

    /**
     * Deletes a singular {@link AExperienceRole experienceRole} directly.
     * @param role The {@link AExperienceRole experienceRole} to delete.
     */
    void unsetRole(AExperienceRole role);

    /**
     * Retrieves the {@link AExperienceRole experienceRole} which uses the given {@link ARole role} in the {@link AServer server}
     * @param role The {@link ARole role} to search for
     * @return the {@link AExperienceRole experienceRole} which uses the given {@link ARole role}
     */
    AExperienceRole getRoleInServer(ARole role);

    /**
     * Retrieves a possible {@link AExperienceRole role}, if it exists, for the given {@link ARole}. Returns an empty  Optional if it does not exist
     * @param role The {@link ARole role} to search for
     * @return An {@link Optional optional} either empty or containing the {@link AExperienceRole role}
     */
    Optional<AExperienceRole> getRoleInServerOptional(ARole role);

    /**
     * Retrieves all {@link AExperienceRole experienceRoles} configured in the given {@link AServer server}
     * @param server The server to retrieve the list of {@link AExperienceRole experienceRoles} for
     * @return A list of {@link AExperienceRole experienceRoles} which are currently configured for the {@link AServer server}
     */
    List<AExperienceRole> getExperienceRolesForServer(AServer server);

    /**
     * Retrieves the {@link AExperienceRole experienceRole} by the given ID of a {@link ARole}.
     * @param experienceRoleId The ID of the {@link ARole role} (which is the same as the {@link AExperienceRole experienceRole}) to retrieve the {@link AExperienceRole experienceRole} for
     * @return The found {@link AExperienceRole experienceRole}
     */
    AExperienceRole getExperienceRoleById(Long experienceRoleId);

    /**
     * Retrieves the {@link AExperienceRole experienceRole} in an {@link Optional optional} by the given ID of a {@link ARole}.
     * @param experienceRoleId The ID of the {@link ARole role} (which is the same as the {@link AExperienceRole experienceRole}) to retrieve the {@link AExperienceRole experienceRole} for
     * @return An {@link Optional optional} containing the found {@link AExperienceRole experienceRole} or empty otherwise
     */
    Optional<AExperienceRole> getExperienceRoleByIdOptional(Long experienceRoleId);
}

package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.exception.AssignedUserNotFoundException;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignedRoleUser;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;

import java.util.Optional;

/**
 * Management service for {@link AssignedRoleUser assignedRoleUser} table
 */
public interface AssignedRoleUserManagementService {
    /**
     * Adds the given {@link AssignableRole assignableRole} to the given {@link AUserInAServer user}.
     * @param assignableRole The {@link AssignableRole assignableRole} to add
     * @param aUserInAServer The {@link AUserInAServer user} who should get the {@link AssignableRole role}
     */
    void addAssignedRoleToUser(AssignableRole assignableRole, AUserInAServer aUserInAServer);

    /**
     * Removes the given {@link AssignableRole assignableFrom} from the given {@link AUserInAServer user}.
     * @param assignableRole The {@link AssignableRole assignableRole} to remove
     * @param aUserInAServer The {@link AUserInAServer user} from whom the {@link AssignableRole role} should be removed
     */
    void removeAssignedRoleFromUser(AssignableRole assignableRole, AUserInAServer aUserInAServer);

    /**
     * Removes the given {@link AssignableRole assignableFrom} from the given {@link AssignedRoleUser user}.
     * @param assignableRole The {@link AssignableRole assignableRole} to remove
     * @param user The {@link AssignedRoleUser user} from whom the {@link AssignableRole role} should be removed
     */
    void removeAssignedRoleFromUser(AssignableRole assignableRole, AssignedRoleUser user);

    /**
     * Creating an {@link AssignedRoleUser assignedRoleUser} from the given {@link AUserInAServer user}
     * @param aUserInAServer The {@link AUserInAServer user} to create it for
     * @return The created instance of {@link AssignedRoleUser assignedRoleUser}
     */
    AssignedRoleUser createAssignedRoleUser(AUserInAServer aUserInAServer);

    /**
     * Tries to retrieve a {@link AssignedRoleUser assignedRoleUser} from an {@link AUserInAServer userInAServer},
     * and returns it as an {@link Optional optional}, if it exists. Empty otherwise.
     * @param aUserInAServer The {@link AUserInAServer user} to search for
     * @return An {@link Optional optional} containing the {@link AssignedRoleUser user}
     */
    Optional<AssignedRoleUser> findByUserInServerOptional(AUserInAServer aUserInAServer);

    /**
     * Tries to retrieve a {@link AssignedRoleUser assignedRoleUser} from an {@link AUserInAServer userInAServer},
     * and returns it as an {@link Optional optional}, if it exists. Empty otherwise.
     * @param serverUser The {@link ServerUser user} to search for
     * @return An {@link Optional optional} containing the {@link AssignedRoleUser user}
     */
    Optional<AssignedRoleUser> findByUserInServerOptional(ServerUser serverUser);

    /**
     * Tries to retrieve a {@link AssignedRoleUser assignedRoleUser} from an {@link AUserInAServer userInAServer},
     * and returns it if it exists. Throws an exception otherwise.
     * @param aUserInAServer The {@link AUserInAServer user} to search for
     * @throws AssignedUserNotFoundException if it doesnt find any
     * @return The {@link AssignedRoleUser user} if it exists
     */
    AssignedRoleUser findByUserInServer(AUserInAServer aUserInAServer);
}

package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignedRoleUser;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible to manage actions on an {@link AssignableRole assignableRole}
 */
public interface AssignableRoleService {

    /**
     * Adds the given {@link AssignableRole assignableRole} to the given {@link Member member}
     * @param assignableRoleId The ID of the {@link AssignableRole} to add
     * @param member The {@link Member member} who should receive the {@link AssignableRole role}
     * @return A {@link CompletableFuture future} which completes when the {@link net.dv8tion.jda.api.entities.Role role} was added to the {@link Member member}
     */

    CompletableFuture<Void> assignAssignableRoleToUser(Long assignableRoleId, Member member);
    CompletableFuture<Void> assignAssignableRoleToUser(Role role, Member member);
    void assignableRoleConditionFailure();

    /**
     * Clears all {@link AssignableRole assignableRoles} which are currently given to the {@link AUserInAServer user} of a certain
     * {@link AssignableRolePlace place}
     * @param place The {@link AssignableRolePlace place} from which all {@link AssignableRole roles} from an {@link AUserInAServer user} should be removed of
     * @param user The {@link AUserInAServer user} to remove the {@link AssignableRole assignableRoles} of
     */
    void clearAllRolesOfUserInPlace(AssignableRolePlace place, AUserInAServer user);

    /**
     * Removes the {@link AssignableRole role} from the given {@link Member member}
     * @param assignableRole The {@link AssignableRole role} to remove
     * @param member The {@link Member member} to remove the {@link AssignableRole role} from
     * @return A {@link CompletableFuture future} which completes when the {@link net.dv8tion.jda.api.entities.Role role}
     *  has been removed from the {@link Member member}
     */
    CompletableFuture<Void> removeAssignableRoleFromUser(AssignableRole assignableRole, Member member);
    CompletableFuture<Void> removeAssignableRoleFromUser(Role role, Member member);

    /**
     * Removes the {@link AssignableRole role} from the given {@link Member member}
     * @param assignableRoleId The ID of an {@link AssignableRole role} to remove
     * @param member The {@link Member member} to remove the {@link AssignableRole role} from
     * @return A {@link CompletableFuture future} which completes when the {@link net.dv8tion.jda.api.entities.Role role}
     *  has been removed from the {@link Member member}
     */
    CompletableFuture<Void> removeAssignableRoleFromUser(Long assignableRoleId, Member member);

    /**
     * Adds the {@link AssignableRole assignableRole} to the given {@link AUserInAServer userInAServer} in the database
     * @param assignableRole The {@link AssignableRole role} to be added
     * @param aUserInAServer The {@link AUserInAServer user} to get the {@link AssignableRole role}
     */
    void addRoleToUser(AssignableRole assignableRole, AUserInAServer aUserInAServer);

    /**
     * Removes the {@link AssignableRole assignableRole} from the given {@link AUserInAServer userInAServer} in the database
     * @param assignableRole The {@link AssignableRole role} to be removed
     * @param aUserInAServer The {@link AUserInAServer user} to get the {@link AssignableRole role} removed
     */
    void removeRoleFromUser(AssignableRole assignableRole, AUserInAServer aUserInAServer);

    AssignableRole getAssignableRoleInPlace(AssignableRolePlace place, Role role);
    AssignableRole getAssignableRoleInPlace(AssignableRolePlace place, ARole role);
    AssignableRole getAssignableRoleInPlace(AssignableRolePlace place, Long roleId);

    void removeAssignableRolesFromAssignableRoleUser(List<AssignableRole> roles, AssignedRoleUser roleUser);

}

package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Member;

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
    /**
     * Adds the given {@link AssignableRole assignableRole} to the given {@link ServerUser serverUser}
     * @param assignableRoleId The ID of the {@link AssignableRole} to add
     * @param serverUser The {@link ServerUser serverUser} who should receive the {@link AssignableRole role}
     * @return A {@link CompletableFuture future} which completes when the {@link net.dv8tion.jda.api.entities.Role role} was added to the {@link ServerUser serverUser}
     */
    CompletableFuture<Void> assignAssignableRoleToUser(Long assignableRoleId, ServerUser serverUser);

    /**
     * Clears all {@link AssignableRole assignableRoles} which are currently given to the {@link AUserInAServer user} of a certain
     * {@link AssignableRolePlace place}
     * @param place The {@link AssignableRolePlace place} from which all {@link AssignableRole roles} from an {@link AUserInAServer user} should be removed of
     * @param user The {@link AUserInAServer user} to remove the {@link AssignableRole assignableRoles} of
     */
    void clearAllRolesOfUserInPlace(AssignableRolePlace place, AUserInAServer user);

    /**
     * Assigns the given {@link AssignableRole role} to the {@link Member member} and stores the assignment as a
     * {@link dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser}
     * @param assignableRoleId The ID of an {@link AssignableRole assignableRole} to be added to the {@link Member member}
     * @param toAdd The {@link Member member} to add the role to
     * @return A {@link CompletableFuture future} which completes when the {@link net.dv8tion.jda.api.entities.Role role} has
     * been added and the {@link dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser user} has been persisted
     */
    CompletableFuture<Void> fullyAssignAssignableRoleToUser(Long assignableRoleId, Member toAdd);

    /**
     * Removes the {@link AssignableRole role} from the given {@link Member member}
     * @param assignableRole The {@link AssignableRole role} to remove
     * @param member The {@link Member member} to remove the {@link AssignableRole role} from
     * @return A {@link CompletableFuture future} which completes when the {@link net.dv8tion.jda.api.entities.Role role}
     *  has been removed from the {@link Member member}
     */
    CompletableFuture<Void> removeAssignableRoleFromUser(AssignableRole assignableRole, Member member);

    /**
     * Removes the {@link AssignableRole role} from the given {@link Member member}
     * @param assignableRoleId The ID of an {@link AssignableRole role} to remove
     * @param member The {@link Member member} to remove the {@link AssignableRole role} from
     * @return A {@link CompletableFuture future} which completes when the {@link net.dv8tion.jda.api.entities.Role role}
     *  has been removed from the {@link Member member}
     */
    CompletableFuture<Void> removeAssignableRoleFromUser(Long assignableRoleId, Member member);

    /**
     * Removes the {@link AssignableRole role} from the given {@link AUserInAServer aUserInAServer}
     * @param assignableRole The {@link AssignableRole role} to remove
     * @param aUserInAServer The {@link AUserInAServer aUserInAServer} to remove the {@link AssignableRole role} from
     * @return A {@link CompletableFuture future} which completes when the {@link net.dv8tion.jda.api.entities.Role role}
     *  has been removed from the  {@link AUserInAServer aUserInAServer}
     */
    CompletableFuture<Void> removeAssignableRoleFromUser(AssignableRole assignableRole, AUserInAServer aUserInAServer);

    /**
     * Removes the given {@link AssignableRole role} from the {@link ServerUser serverUser} and stores the assignment as a
     * {@link dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser}
     * @param assignableRole The {@link AssignableRole assignableRole} to be removed from the {@link ServerUser serverUser}
     * @param serverUser The {@link ServerUser serverUser} to remove the role from
     * @return A {@link CompletableFuture future} which completes when the {@link net.dv8tion.jda.api.entities.Role role} has
     * been removed and the {@link dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser user} has been persisted
     */
    CompletableFuture<Void> fullyRemoveAssignableRoleFromUser(AssignableRole assignableRole, ServerUser serverUser);

    /**
     * Adds the {@link AssignableRole assignableRole} to the given {@link AUserInAServer userInAServer} in the database
     * @param assignableRoleId The ID of the {@link AssignableRole role} to be added
     * @param aUserInAServer The {@link AUserInAServer user} to get the {@link AssignableRole role}
     */
    void addRoleToUser(Long assignableRoleId, AUserInAServer aUserInAServer);

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

    /**
     * Removes the {@link AssignableRole assignableRole} from the given {@link AUserInAServer userInAServer} in the database
     * @param assignableRoleId The ID of the {@link AssignableRole role} to be removed
     * @param aUserInAServer The {@link AUserInAServer user} to get the {@link AssignableRole role} removed
     */
    void removeRoleFromUser(Long assignableRoleId, AUserInAServer aUserInAServer);
}

package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.CompletableFuture;

public interface AssignableRoleService {
    CompletableFuture<Void> assignAssignableRoleToUser(Long assignableRoleId, Member toAdd);
    void clearAllRolesOfUserInPlace(AssignableRolePlace place, AUserInAServer user);
    CompletableFuture<Void> fullyAssignAssignableRoleToUser(Long assignableRoleId, Member toAdd);
    CompletableFuture<Void> removeAssignableRoleFromUser(AssignableRole assignableRole, Member member);
    CompletableFuture<Void> removeAssignableRoleFromUser(Long assignableRoleId, Member member);
    CompletableFuture<Void> removeAssignableRoleFromUser(AssignableRole assignableRole, AUserInAServer aUserInAServer);
    CompletableFuture<Void> fullyRemoveAssignableRoleFromUser(AssignableRole assignableRole, Member member);
    void addRoleToUser(Long assignableRoleId, AUserInAServer aUserInAServer);
    void addRoleToUser(AssignableRole assignableRole, AUserInAServer aUserInAServer);
    void removeRoleFromUser(AssignableRole assignableRole, AUserInAServer aUserInAServer);
    void removeRoleFromUser(Long assignableRoleId, AUserInAServer aUserInAServer);
}

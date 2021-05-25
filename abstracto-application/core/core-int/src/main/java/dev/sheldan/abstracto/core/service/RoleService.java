package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface RoleService {
    void addRoleToUser(AUserInAServer aUserInAServer, ARole role);
    CompletableFuture<Void> addRoleToUserFuture(AUserInAServer aUserInAServer, ARole role);
    CompletableFuture<Void> addRoleToMemberFuture(Member member, Long roleId);
    void addRoleToMember(Member member, ARole role);
    CompletableFuture<Void> addRoleToMemberFuture(Member member, ARole role);
    void removeRoleFromMember(Member member, ARole role);
    CompletableFuture<Void> removeRoleFromMemberAsync(Member member, ARole role);
    CompletableFuture<Void> removeRoleFromMemberAsync(Member member, Long roleId);
    CompletableFuture<Void> addRoleToUser(Guild guild, Long userId, Role roleById);
    CompletableFuture<Void> removeRoleFromUser(Guild guild, Long userId, Role roleById);
    void removeRoleFromUser(AUserInAServer aUserInAServer, ARole role);
    CompletableFuture<Void> removeRoleFromUserFuture(AUserInAServer aUserInAServer, ARole role);
    void markDeleted(Role role, AServer server);
    void markDeleted(Long id, AServer server);
    Role getRoleFromGuild(ARole role);
    List<Role> getRolesFromGuild(List<ARole> roles);
    boolean hasAnyOfTheRoles(Member member, List<ARole> roles);
    Optional<ARole> getFirstRole(Member member, List<ARole> roles);
    boolean memberHasRole(Member member, Role role);
    boolean memberHasRole(Member member, ARole role);
    boolean memberHasRole(Member member, Long roleId);
    boolean isRoleInServer(ARole role);
    boolean canBotInteractWithRole(ARole role);
    ARole getFakeRoleFromRole(Role role);
    ARole getFakeRoleFromId(Long roleId);
}

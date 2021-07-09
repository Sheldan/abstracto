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
    void addRoleToMember(AUserInAServer aUserInAServer, ARole role);
    CompletableFuture<Void> addRoleToUserAsync(AUserInAServer aUserInAServer, ARole role);
    CompletableFuture<Void> addRoleToMemberAsync(Member member, Long roleId);
    CompletableFuture<Void> addRoleToMemberAsync(Member member, Role role);
    void addRoleToMember(Member member, ARole role);
    CompletableFuture<Void> addRoleToMemberAsync(Member member, ARole role);
    void removeRoleFromMember(Member member, ARole role);
    CompletableFuture<Void> removeRoleFromMemberAsync(Member member, ARole role);
    CompletableFuture<Void> removeRoleFromMemberAsync(Member member, Long roleId);
    CompletableFuture<Void> addRoleToMemberAsync(Guild guild, Long userId, Role roleById);
    CompletableFuture<Void> removeRoleFromUserAsync(Guild guild, Long userId, Role roleById);
    void removeRoleFromUser(AUserInAServer aUserInAServer, ARole role);
    CompletableFuture<Void> removeRoleFromUserAsync(AUserInAServer aUserInAServer, ARole role);
    CompletableFuture<Void> removeRoleFromUserAsync(Member member, Role role);
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
    boolean canBotInteractWithRole(Role role);
    ARole getFakeRoleFromRole(Role role);
    ARole getFakeRoleFromId(Long roleId);
}

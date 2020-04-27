package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public interface RoleService {
    void addRoleToUser(AUserInAServer aUserInAServer, ARole role);
    void removeRoleFromUser(AUserInAServer aUserInAServer, ARole role);
    void markDeleted(Role role);
    void markDeleted(Long id);
    Role getRoleFromGuild(ARole role);
    List<Role> getRolesFromGuild(List<ARole> roles);
    boolean memberHasRole(Member member, Role role);
    boolean memberHasRole(Member member, ARole role);
    boolean isRoleInServer(ARole role);
}

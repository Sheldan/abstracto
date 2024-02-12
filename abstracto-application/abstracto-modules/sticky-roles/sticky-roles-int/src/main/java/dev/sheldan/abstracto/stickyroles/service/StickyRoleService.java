package dev.sheldan.abstracto.stickyroles.service;

import dev.sheldan.abstracto.stickyroles.model.database.StickyRole;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public interface StickyRoleService {
    default void ignoreRoleFromStickyRoles(Role role) {
        setRoleStickiness(role, false);
    }
    default void addRoleToStickyRoles(Role role) {
        setRoleStickiness(role, true);
    }
    void setRoleStickiness(Role role, Boolean stickiness);

    void setStickiness(Member member, Boolean newState);
    void setStickiness(User user, Guild guild, Boolean newState);

    void handleLeave(Member member);

    void handleJoin(Member member);

    List<StickyRole> getStickyRolesForServer(Guild guild);
}

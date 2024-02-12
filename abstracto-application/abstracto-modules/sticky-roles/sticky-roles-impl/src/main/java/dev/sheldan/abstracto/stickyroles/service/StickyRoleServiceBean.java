package dev.sheldan.abstracto.stickyroles.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.stickyroles.model.database.StickyRole;
import dev.sheldan.abstracto.stickyroles.model.database.StickyRoleUser;
import dev.sheldan.abstracto.stickyroles.service.management.StickyRoleManagementService;
import dev.sheldan.abstracto.stickyroles.service.management.StickyRoleUserManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StickyRoleServiceBean implements StickyRoleService {

    @Autowired
    private StickyRoleManagementService stickyRoleManagementService;

    @Autowired
    private StickyRoleUserManagementService stickyRoleUserManagementService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public void setRoleStickiness(Role role, Boolean stickiness) {
        StickyRole stickyRole = stickyRoleManagementService.getOrCreateStickyRole(role);
        log.info("Setting stickiness of role {} in server {} to {}", role.getIdLong(), role.getGuild().getIdLong(), stickiness);
        stickyRole.setSticky(stickiness);
    }

    @Override
    public void setStickiness(Member member, Boolean stickiness) {
        StickyRoleUser user = stickyRoleUserManagementService.getOrCreateStickyRoleUser(member);
        user.setSticky(stickiness);
        log.info("Setting stickiness of member {} in server {} to {}", member.getIdLong(), member.getGuild().getIdLong(), stickiness);
        if(!stickiness) {
            clearStickyRolesForUser(user);
        }
    }

    @Override
    public void setStickiness(User user, Guild guild, Boolean stickiness) {
        StickyRoleUser stickyUser = stickyRoleUserManagementService.getOrCreateStickyRoleUser(guild.getIdLong(), user.getIdLong());
        stickyUser.setSticky(stickiness);
        log.info("Setting stickiness of member {} in server {} to {}", user.getIdLong(), guild.getIdLong(), stickiness);
        if(!stickiness) {
            clearStickyRolesForUser(stickyUser);
        }
    }

    @Override
    public void handleLeave(Member member) {
        log.info("Handling user leave of member {} from server {} regarding sticky roles.", member.getIdLong(), member.getGuild().getIdLong());
        StickyRoleUser user = stickyRoleUserManagementService.getOrCreateStickyRoleUser(member);
        clearStickyRolesForUser(user);
        if(user.getSticky()) {
            List<Role> memberRoles = member.getRoles();
            log.info("Member was marked as sticky - storing {} roles.", memberRoles.size());
            Set<Long> memberRoleIds = memberRoles
                    .stream()
                    .map(ISnowflake::getIdLong)
                    .collect(Collectors.toSet());
            List<StickyRole> existingStickyRolesOfUser = stickyRoleManagementService.getRoles(new ArrayList<>(memberRoleIds));
            Set<Long> existingStickyRoleIdsOfUser = existingStickyRolesOfUser
                    .stream()
                    .map(StickyRole::getId)
                    .collect(Collectors.toSet());
            memberRoleIds.removeAll(existingStickyRoleIdsOfUser);
            List<StickyRole> newStickyRoles = memberRoleIds
                    .stream()
                    .map(rid -> stickyRoleManagementService.createStickyRole(rid))
                    .toList();
            log.debug("Creating {} new roles.", newStickyRoles.size());
            List<StickyRole> stickyRolesOfUser = new ArrayList<>(existingStickyRolesOfUser);
            stickyRolesOfUser.addAll(newStickyRoles);
            stickyRolesOfUser.forEach(stickyRole -> {
                stickyRole.getUsers().add(user);
            });
            user.setRoles(stickyRolesOfUser);
        }
    }

    private static void clearStickyRolesForUser(StickyRoleUser user) {
        log.debug("Clearing sticky roles for user {}", user.getId());
        user.getRoles().forEach(stickyRole -> {
            stickyRole.getUsers().remove(user);
        });
        user.getRoles().clear();
    }

    @Override
    public void handleJoin(Member member) {
        log.info("Handling server join for member {} in server {} regarding sticky roles.", member.getIdLong(), member.getGuild().getIdLong());
        StickyRoleUser user = stickyRoleUserManagementService.getOrCreateStickyRoleUser(member);
        if(user.getSticky()) {
            List<Long> rolesToAdd = user
                    .getRoles()
                    .stream()
                    .filter(StickyRole::getSticky)
                    .filter(r -> Boolean.FALSE.equals(r.getRole().getDeleted()))
                    .map(StickyRole::getId)
                    .toList();
            log.info("Adding {} roles to user {} in server {}", rolesToAdd.size(), member.getIdLong(), member.getGuild().getIdLong());
            roleService.updateRolesIds(member, new ArrayList<>(), rolesToAdd).thenAccept(unused -> {
                log.info("Successfully added {} roles to user {} in server {}", rolesToAdd.size(), member.getIdLong(), member.getGuild().getIdLong());
            }).exceptionally(throwable -> {
                log.warn("Failed to add {} roles to user {} in server {}", rolesToAdd.size(), member.getIdLong(), member.getGuild().getIdLong(), throwable);
                return null;
            });
        } else {
            log.info("Not re-applying roles for member {} in server {} as they opted out.", member.getIdLong(), member.getGuild().getIdLong());
        }
    }

    @Override
    public List<StickyRole> getStickyRolesForServer(Guild guild) {
        AServer server = serverManagementService.loadServer(guild);
        return stickyRoleManagementService.getStickyRolesForServer(server);
    }
}

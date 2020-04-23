package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.core.exception.RoleException;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class RoleServiceBean implements RoleService {

    @Autowired
    private BotService botService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Override
    public void addRoleToUser(AUserInAServer aUserInAServer, ARole role) {
        Optional<Guild> guildById = botService.getGuildById(aUserInAServer.getServerReference().getId());
        if(guildById.isPresent()) {
            Guild guild = guildById.get();
            Role roleById = guild.getRoleById(role.getId());
            if(roleById != null) {
                guild.addRoleToMember(aUserInAServer.getUserReference().getId(), roleById).queue();
            } else {
                throw new RoleException(String.format("Failed to load role %s in guild %s", role.getId(), aUserInAServer.getServerReference().getId()));
            }
        } else {
            throw new GuildException(String.format("Failed to load guild %s.", aUserInAServer.getServerReference().getId()));
        }
    }

    @Override
    public void removeRoleFromUser(AUserInAServer aUserInAServer, ARole role) {
        Optional<Guild> guildById = botService.getGuildById(aUserInAServer.getServerReference().getId());
        if(guildById.isPresent()) {
            Guild guild = guildById.get();
            Role roleById = guild.getRoleById(role.getId());
            if(roleById != null) {
                guild.removeRoleFromMember(aUserInAServer.getUserReference().getId(), roleById).queue();
            } else {
                throw new RoleException(String.format("Failed to load role %s in guild %s", role.getId(), aUserInAServer.getServerReference().getId()));
            }
        } else {
            throw new GuildException(String.format("Failed to load guild %s.", aUserInAServer.getServerReference().getId()));
        }
    }

    @Override
    public void markDeleted(Role role) {
        markDeleted(role.getIdLong());
    }

    @Override
    public void markDeleted(Long id) {
        ARole role = roleManagementService.findRole(id);
        if(role != null) {
            roleManagementService.markDeleted(role);
        } else {
            throw new RoleException(String.format("Cannot find role %s to mark as deleted.", id));
        }
    }

    @Override
    public Role getRoleFromGuild(ARole role) {
        Optional<Guild> guildById = botService.getGuildById(role.getServer().getId());
        if(guildById.isPresent()) {
            return guildById.get().getRoleById(role.getId());
        } else {
            throw new GuildException(String.format("Failed to load guild %s.", role.getServer().getId()));
        }
    }

    @Override
    public boolean memberHasRole(Member member, Role role) {
        return member.getRoles().stream().anyMatch(role1 -> role1.getIdLong() == role.getIdLong());
    }

    @Override
    public boolean memberHasRole(Member member, ARole role) {
        return member.getRoles().stream().anyMatch(role1 -> role1.getIdLong() == role.getId());
    }

    @Override
    public boolean isRoleInServer(ARole role) {
       return getRoleFromGuild(role) != null;
    }
}

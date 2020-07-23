package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.exception.RoleDeletedException;
import dev.sheldan.abstracto.core.exception.RoleNotFoundInDBException;
import dev.sheldan.abstracto.core.exception.RoleNotFoundInGuildException;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
            addRoleToUser(guildById.get(), role, aUserInAServer.getUserReference().getId());
        } else {
            throw new GuildNotFoundException(aUserInAServer.getServerReference().getId());
        }
    }

    @Override
    public CompletableFuture<Void> addRoleToUserFuture(AUserInAServer aUserInAServer, ARole role) {
        Optional<Guild> guildById = botService.getGuildById(aUserInAServer.getServerReference().getId());
        if(guildById.isPresent()) {
            return addRoleToUserFuture(guildById.get(), role, aUserInAServer.getUserReference().getId());
        } else {
            throw new GuildNotFoundException(aUserInAServer.getServerReference().getId());
        }
    }

    @Override
    public void addRoleToMember(Member member, ARole role) {
        Guild guild = member.getGuild();
        addRoleToUser(guild, role, member.getIdLong());
    }

    @Override
    public CompletableFuture<Void> addRoleToMemberFuture(Member member, ARole role) {
        Guild guild = member.getGuild();
        return addRoleToUserFuture(guild, role, member.getIdLong());
    }

    @Override
    public void removeRoleFromMember(Member member, ARole role) {
        Guild guild = member.getGuild();
        removeRoleFromUser(guild, role, member.getIdLong());
    }

    @Override
    public CompletableFuture<Void> removeRoleFromMemberFuture(Member member, ARole role) {
        Guild guild = member.getGuild();
        return removeRoleFromUserFuture(guild, role, member.getIdLong());
    }

    private CompletableFuture<Void> addRoleToUserFuture(Guild guild, ARole role,  Long userId) {
        if(role.getDeleted()) {
            log.warn("Not possible to add role to user. Role {} was marked as deleted.", role.getId());
            throw new RoleDeletedException(role);
        }
        Role roleById = guild.getRoleById(role.getId());
        if(roleById != null) {
            return guild.addRoleToMember(userId, roleById).submit();
        } else {
            throw new RoleNotFoundInGuildException(role.getId(), guild.getIdLong());
        }
    }


    private void addRoleToUser(Guild guild, ARole role,  Long userId) {
        addRoleToUserFuture(guild, role, userId);
    }

    private CompletableFuture<Void> removeRoleFromUserFuture(Guild guild, ARole role,  Long userId) {
        if(role.getDeleted()) {
            log.warn("Not possible to remove role from user. Role {} was marked as deleted.", role.getId());
            throw new RoleDeletedException(role);
        }
        Role roleById = guild.getRoleById(role.getId());
        if(roleById != null) {
            return guild.removeRoleFromMember(userId, roleById).submit();
        } else {
            throw new RoleNotFoundInGuildException(role.getId(), guild.getIdLong());
        }
    }


    private void removeRoleFromUser(Guild guild, ARole role,  Long userId) {
       removeRoleFromUserFuture(guild, role, userId);
    }

    @Override
    public void removeRoleFromUser(AUserInAServer aUserInAServer, ARole role) {
        Optional<Guild> guildById = botService.getGuildById(aUserInAServer.getServerReference().getId());
        if(guildById.isPresent()) {
            removeRoleFromUser(guildById.get(), role, aUserInAServer.getUserReference().getId());
        } else {
            throw new GuildNotFoundException(aUserInAServer.getServerReference().getId());
        }
    }

    @Override
    public CompletableFuture<Void> removeRoleFromUserFuture(AUserInAServer aUserInAServer, ARole role) {
        Optional<Guild> guildById = botService.getGuildById(aUserInAServer.getServerReference().getId());
        if(guildById.isPresent()) {
            return removeRoleFromUserFuture(guildById.get(), role, aUserInAServer.getUserReference().getId());
        } else {
            throw new GuildNotFoundException(aUserInAServer.getServerReference().getId());
        }
    }

    @Override
    public void markDeleted(Role role, AServer server) {
        markDeleted(role.getIdLong(), server);
    }

    @Override
    public void markDeleted(Long id, AServer server) {
        Optional<ARole> role = roleManagementService.findRoleOptional(id);
        ARole role1 = role.orElseThrow(() -> new RoleNotFoundInDBException(id));
        roleManagementService.markDeleted(role1);
    }

    @Override
    public Role getRoleFromGuild(ARole role) {
        if(role.getDeleted()) {
            log.warn("Trying to load role {} which is marked as deleted.", role.getId());
            throw new RoleDeletedException(role);
        }
        Optional<Guild> guildById = botService.getGuildById(role.getServer().getId());
        if(guildById.isPresent()) {
            return guildById.get().getRoleById(role.getId());
        } else {
            throw new GuildNotFoundException(role.getServer().getId());
        }
    }

    @Override
    public List<Role> getRolesFromGuild(List<ARole> roles) {
        return roles.stream().map(this::getRoleFromGuild).collect(Collectors.toList());
    }

    @Override
    public boolean hasAnyOfTheRoles(Member member, List<ARole> roles) {
        return member.getRoles().stream().anyMatch(role1 -> roles.stream().anyMatch(role -> role.getId() == role1.getIdLong()));
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

    @Override
    public boolean canBotInteractWithRole(ARole role) {
        Role jdaRole = getRoleFromGuild(role);
        Member selfMember = jdaRole.getGuild().getSelfMember();
        return selfMember.canInteract(jdaRole);
    }
}

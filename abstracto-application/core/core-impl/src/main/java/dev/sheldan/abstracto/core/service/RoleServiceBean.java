package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.exception.RoleDeletedException;
import dev.sheldan.abstracto.core.exception.RoleNotFoundInDBException;
import dev.sheldan.abstracto.core.exception.RoleNotFoundInGuildException;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static dev.sheldan.abstracto.core.config.MetricConstants.DISCORD_API_INTERACTION_METRIC;
import static dev.sheldan.abstracto.core.config.MetricConstants.INTERACTION_TYPE;

@Component
@Slf4j
public class RoleServiceBean implements RoleService {

    @Autowired
    private GuildService guildService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private MetricService metricService;

    public static final CounterMetric ROLE_ASSIGNED_METRIC = CounterMetric
            .builder()
            .name(DISCORD_API_INTERACTION_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(INTERACTION_TYPE, "role.assigned")))
            .build();


    public static final CounterMetric ROLE_REMOVED_METRIC = CounterMetric
            .builder()
            .name(DISCORD_API_INTERACTION_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(INTERACTION_TYPE, "role.removed")))
            .build();

    @Override
    public void addRoleToMember(AUserInAServer aUserInAServer, ARole role) {
        Optional<Guild> guildById = guildService.getGuildByIdOptional(aUserInAServer.getServerReference().getId());
        if(guildById.isPresent()) {
            addRoleToMember(guildById.get(), role, aUserInAServer.getUserReference().getId());
        } else {
            throw new GuildNotFoundException(aUserInAServer.getServerReference().getId());
        }
    }

    @Override
    public CompletableFuture<Void> addRoleToUserAsync(AUserInAServer aUserInAServer, ARole role) {
        Optional<Guild> guildById = guildService.getGuildByIdOptional(aUserInAServer.getServerReference().getId());
        if(guildById.isPresent()) {
            return addRoleToUserAsync(guildById.get(), aUserInAServer.getUserReference().getId(), role);
        } else {
            throw new GuildNotFoundException(aUserInAServer.getServerReference().getId());
        }
    }

    @Override
    public CompletableFuture<Void> addRoleToMemberAsync(Member member, Long roleId) {
        Role role = member.getGuild().getRoleById(roleId);
        if(role == null) {
            throw new RoleNotFoundInGuildException(roleId, member.getGuild().getIdLong());
        }
        return addRoleToMemberAsync(member.getGuild(), member.getIdLong(), role);
    }

    @Override
    public CompletableFuture<Void> updateRolesIds(Member member, List<Long> rolesToRemove, List<Long> rolesToAdd) {
        List<Role> rolesObjToRemove = rolesToRemove
                .stream().map(aLong -> member.getGuild().getRoleById(aLong))
                .toList();

        List<Role> rolesObjToAdd = rolesToAdd
                .stream().map(aLong -> member.getGuild().getRoleById(aLong))
                .toList();
        return updateRolesObj(member, rolesObjToRemove, rolesObjToAdd);
    }

    @Override
    public CompletableFuture<Void> updateRolesObj(Member member, List<Role> rolesToRemove, List<Role> rolesToAdd) {
        return member.getGuild().modifyMemberRoles(member, rolesToAdd, rolesToRemove).submit();
    }

    @Override
    public CompletableFuture<Void> addRoleToMemberAsync(Member member, Role role) {
        return addRoleToMemberAsync(member.getGuild(), member.getIdLong(), role);
    }


    @Override
    public void addRoleToMember(Member member, ARole role) {
        addRoleToMemberAsync(member, role.getId());
    }

    @Override
    public CompletableFuture<Void> addRoleToMemberAsync(Member member, ARole role) {
        return addRoleToMemberAsync(member, role.getId());
    }

    @Override
    public void removeRoleFromMember(Member member, ARole role) {
        removeRoleFromMemberAsync(member, role.getId());
    }

    @Override
    public CompletableFuture<Void> removeRoleFromMemberAsync(Member member, ARole role) {
        Guild guild = member.getGuild();
        return removeRoleFromUserFuture(guild, role, member.getIdLong());
    }

    @Override
    public CompletableFuture<Void> removeRoleFromMemberAsync(Member member, Long roleId) {
        Role role = member.getGuild().getRoleById(roleId);
        if(role == null) {
            throw new RoleNotFoundInGuildException(roleId, member.getGuild().getIdLong());
        }
        return member.getGuild().removeRoleFromMember(member, role).submit();
    }

    private CompletableFuture<Void> addRoleToUserAsync(Guild guild, Long userId, ARole role) {
        Role roleById = guild.getRoleById(role.getId());
        if(roleById != null) {
            log.info("Adding role {} to user {} in server {}.", role.getId(), userId, guild.getId());
            return addRoleToMemberAsync(guild, userId, roleById);
        } else {
            throw new RoleNotFoundInGuildException(role.getId(), guild.getIdLong());
        }
    }

    @Override
    public CompletableFuture<Void> addRoleToMemberAsync(Guild guild, Long userId, Role roleById) {
        metricService.incrementCounter(ROLE_ASSIGNED_METRIC);
        return guild.addRoleToMember(UserSnowflake.fromId(userId), roleById).submit();
    }

    @Override
    public CompletableFuture<Void> removeRoleFromUserAsync(Guild guild, Long userId, Role roleById) {
        metricService.incrementCounter(ROLE_REMOVED_METRIC);
        return guild.removeRoleFromMember(UserSnowflake.fromId(userId), roleById).submit();
    }


    private void addRoleToMember(Guild guild, ARole role, Long userId) {
        addRoleToUserAsync(guild, userId, role);
    }

    private CompletableFuture<Void> removeRoleFromUserFuture(Guild guild, ARole role,  Long userId) {
        if(role.getDeleted()) {
            log.warn("Not possible to remove role from user. Role {} was marked as deleted.", role.getId());
            throw new RoleDeletedException(role);
        }
        Role roleById = guild.getRoleById(role.getId());
        if(roleById != null) {
            log.info("Removing role {} from user {} in server {}.", role.getId(), userId, guild.getId());
            return removeRoleFromUserAsync(guild, userId, roleById);
        } else {
            throw new RoleNotFoundInGuildException(role.getId(), guild.getIdLong());
        }
    }


    private void removeRoleFromUser(Guild guild, ARole role,  Long userId) {
       removeRoleFromUserFuture(guild, role, userId);
    }

    @Override
    public void removeRoleFromUser(AUserInAServer aUserInAServer, ARole role) {
        Optional<Guild> guildById = guildService.getGuildByIdOptional(aUserInAServer.getServerReference().getId());
        if(guildById.isPresent()) {
            removeRoleFromUser(guildById.get(), role, aUserInAServer.getUserReference().getId());
        } else {
            throw new GuildNotFoundException(aUserInAServer.getServerReference().getId());
        }
    }

    @Override
    public CompletableFuture<Void> removeRoleFromUserAsync(AUserInAServer aUserInAServer, ARole role) {
        Optional<Guild> guildById = guildService.getGuildByIdOptional(aUserInAServer.getServerReference().getId());
        if(guildById.isPresent()) {
            return removeRoleFromUserFuture(guildById.get(), role, aUserInAServer.getUserReference().getId());
        } else {
            throw new GuildNotFoundException(aUserInAServer.getServerReference().getId());
        }
    }

    @Override
    public CompletableFuture<Void> removeRoleFromUserAsync(Member member, Role role) {
        return removeRoleFromUserAsync(member.getGuild(), member.getIdLong(), role);
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
        Optional<Guild> guildById = guildService.getGuildByIdOptional(role.getServer().getId());
        if(guildById.isPresent()) {
            log.debug("Loading role {} from server {}.", role.getId(), role.getServer().getId());
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
    public Optional<ARole> getFirstRole(Member member, List<ARole> roles) {
        return roles.stream().filter(role -> member.getRoles().stream().anyMatch(role1 -> role1.getIdLong() == role.getId())).findFirst();
    }

    @Override
    public boolean memberHasRole(Member member, Role role) {
        return memberHasRole(member, role.getIdLong());
    }

    @Override
    public boolean memberHasRole(Member member, ARole role) {
        return memberHasRole(member, role.getId());
    }

    @Override
    public boolean memberHasRole(Member member, Long roleId) {
        return member.getRoles().stream().anyMatch(role1 -> role1.getIdLong() == roleId);
    }

    @Override
    public boolean isRoleInServer(ARole role) {
       return getRoleFromGuild(role) != null;
    }

    @Override
    public boolean canBotInteractWithRole(ARole role) {
        Role jdaRole = getRoleFromGuild(role);
        return canBotInteractWithRole(jdaRole);
    }

    @Override
    public boolean canBotInteractWithRole(Role role) {
        Member selfMember = role.getGuild().getSelfMember();
        return selfMember.canInteract(role);
    }

    @Override
    public ARole getFakeRoleFromRole(Role role) {
        AServer server = AServer
                .builder()
                .id(role.getGuild().getIdLong())
                .fake(true)
                .build();
        return ARole
                .builder()
                .fake(true)
                .deleted(false)
                .id(role.getIdLong())
                .server(server)
                .build();
    }

    @Override
    public ARole getFakeRoleFromId(Long roleId) {
        return ARole.builder().id(roleId).fake(true).build();
    }

    @Override
    public CompletableFuture<Void> setRoleColorTo(Role role, Color color) {
        return role.getManager().setColor(color).submit();
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(ROLE_ASSIGNED_METRIC, "Amount of roles assigned");
        metricService.registerCounter(ROLE_REMOVED_METRIC, "Amount of roles removed");
    }
}

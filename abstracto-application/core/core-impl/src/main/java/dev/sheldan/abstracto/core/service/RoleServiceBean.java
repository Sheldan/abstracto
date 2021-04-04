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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
    public void addRoleToUser(AUserInAServer aUserInAServer, ARole role) {
        Optional<Guild> guildById = guildService.getGuildByIdOptional(aUserInAServer.getServerReference().getId());
        if(guildById.isPresent()) {
            addRoleToUser(guildById.get(), role, aUserInAServer.getUserReference().getId());
        } else {
            throw new GuildNotFoundException(aUserInAServer.getServerReference().getId());
        }
    }

    @Override
    public CompletableFuture<Void> addRoleToUserFuture(AUserInAServer aUserInAServer, ARole role) {
        Optional<Guild> guildById = guildService.getGuildByIdOptional(aUserInAServer.getServerReference().getId());
        if(guildById.isPresent()) {
            return addRoleToUserFuture(guildById.get(), aUserInAServer.getUserReference().getId(), role);
        } else {
            throw new GuildNotFoundException(aUserInAServer.getServerReference().getId());
        }
    }

    @Override
    public CompletableFuture<Void> addRoleToMemberFuture(Member member, Long roleId) {
        Role role = member.getGuild().getRoleById(roleId);
        if(role == null) {
            throw new RoleNotFoundInGuildException(roleId, member.getGuild().getIdLong());
        }
        return addRoleToUser(member.getGuild(), member.getIdLong(), role);
    }


    @Override
    public void addRoleToMember(Member member, ARole role) {
        addRoleToMemberFuture(member, role.getId());
    }

    @Override
    public CompletableFuture<Void> addRoleToMemberFuture(Member member, ARole role) {
        return addRoleToMemberFuture(member, role.getId());
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

    private CompletableFuture<Void> addRoleToUserFuture(Guild guild, Long userId, ARole role) {
        if(role.getDeleted()) {
            log.warn("Not possible to add role to user. Role {} was marked as deleted.", role.getId());
            throw new RoleDeletedException(role);
        }
        Role roleById = guild.getRoleById(role.getId());
        if(roleById != null) {
            log.info("Adding role {} to user {} in server {}.", role.getId(), userId, guild.getId());
            return addRoleToUser(guild, userId, roleById);
        } else {
            throw new RoleNotFoundInGuildException(role.getId(), guild.getIdLong());
        }
    }

    @Override
    public CompletableFuture<Void> addRoleToUser(Guild guild, Long userId, Role roleById) {
        metricService.incrementCounter(ROLE_ASSIGNED_METRIC);
        return guild.addRoleToMember(userId, roleById).submit();
    }

    @Override
    public CompletableFuture<Void> removeRoleFromUser(Guild guild, Long userId, Role roleById) {
        metricService.incrementCounter(ROLE_REMOVED_METRIC);
        return guild.removeRoleFromMember(userId, roleById).submit();
    }


    private void addRoleToUser(Guild guild, ARole role,  Long userId) {
        addRoleToUserFuture(guild, userId, role);
    }

    private CompletableFuture<Void> removeRoleFromUserFuture(Guild guild, ARole role,  Long userId) {
        if(role.getDeleted()) {
            log.warn("Not possible to remove role from user. Role {} was marked as deleted.", role.getId());
            throw new RoleDeletedException(role);
        }
        Role roleById = guild.getRoleById(role.getId());
        if(roleById != null) {
            log.info("Removing role {} from user {} in server {}.", role.getId(), userId, guild.getId());
            return removeRoleFromUser(guild, userId, roleById);
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
    public CompletableFuture<Void> removeRoleFromUserFuture(AUserInAServer aUserInAServer, ARole role) {
        Optional<Guild> guildById = guildService.getGuildByIdOptional(aUserInAServer.getServerReference().getId());
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
        Member selfMember = jdaRole.getGuild().getSelfMember();
        return selfMember.canInteract(jdaRole);
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

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(ROLE_ASSIGNED_METRIC, "Amount of roles assigned");
        metricService.registerCounter(ROLE_REMOVED_METRIC, "Amount of roles removed");
    }
}

package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.model.template.LevelRole;
import dev.sheldan.abstracto.experience.model.template.UserSyncStatusModel;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ExperienceRoleServiceBean implements ExperienceRoleService {

    @Autowired
    private ExperienceRoleManagementService experienceRoleManagementService;

    @Autowired
    private ExperienceLevelManagementService experienceLevelService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private MessageService messageService;

    @Override
    public CompletableFuture<Void> setRoleToLevel(Role role, Integer level, GuildMessageChannel messageChannel) {
        ARole aRoleToSet = roleManagementService.findRole(role.getIdLong());
        List<AExperienceRole> experienceRoles = getExperienceRolesAtLevel(level, aRoleToSet.getServer());
        List<ARole> rolesToUnset = experienceRoles
                .stream()
                .map(AExperienceRole::getRole)
                .collect(Collectors.toList());
        if(rolesToUnset.size() == 1 && rolesToUnset.contains(aRoleToSet)) {
            return CompletableFuture.completedFuture(null);
        }
        AExperienceLevel experienceLevel;
        if(!experienceRoles.isEmpty()) {
            experienceLevel = experienceRoles.get(0).getLevel();
        } else {
            experienceLevel = experienceLevelService.getLevel(level);
        }
        experienceRoleManagementService.setLevelToRole(experienceLevel, aRoleToSet);
        if(!rolesToUnset.isEmpty()) {
            return unsetRoles(rolesToUnset, messageChannel);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> unsetRoles(ARole role, GuildMessageChannel messageChannel) {
        return unsetRoles(Arrays.asList(role), messageChannel);
    }

    @Override
    public List<AExperienceRole> getExperienceRolesAtLevel(Integer level, AServer server) {
        AExperienceLevel levelObj = experienceLevelService.getLevel(level);
        return experienceRoleManagementService.getExperienceRolesAtLevelInServer(levelObj, server);
    }


    @Override
    public CompletableFuture<Void> unsetRoles(List<ARole> rolesToUnset, GuildMessageChannel messageChannel) {
        List<AExperienceRole> rolesInServer = experienceRoleManagementService.getRolesInServer(rolesToUnset);
        Integer totalCount = 0;
        for (AExperienceRole aExperienceRole : rolesInServer) {
            totalCount += aExperienceRole.getUsers().size();
        }
        AtomicInteger totalCountAtomic = new AtomicInteger(totalCount);
        long serverId = messageChannel.getGuild().getIdLong();
        MessageToSend status = getUserSyncStatusUpdateModel(0, totalCount, serverId);
        Message statusMessage = messageService.createStatusMessage(status, messageChannel).join();
        AtomicInteger atomicInteger = new AtomicInteger();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        rolesInServer.forEach(experienceRole -> {
            experienceRole.getUsers().forEach(aUserExperience -> {
                futures.add(roleService.removeRoleFromUserAsync(aUserExperience.getUser(), experienceRole.getRole()).thenAccept(unused -> {
                    atomicInteger.set(atomicInteger.get() + 1);
                    log.debug("Finished synchronizing {} users.", atomicInteger.get());
                    if(atomicInteger.get() % 50 == 0) {
                        log.info("Notifying for {} current users with synchronize.", atomicInteger.get());
                        MessageToSend newStatus = getUserSyncStatusUpdateModel(atomicInteger.get(), totalCountAtomic.get(), serverId);
                        messageService.updateStatusMessage(messageChannel, statusMessage.getIdLong(), newStatus);
                    }
                }));
            });
        });
        CompletableFuture<Void> returningFuture = new CompletableFuture<>();
        experienceRoleManagementService.unsetRoles(rolesInServer);
        FutureUtils.toSingleFutureGeneric(futures).whenComplete((unused, throwable) -> {
            MessageToSend newStatus = getUserSyncStatusUpdateModel(atomicInteger.get(), totalCountAtomic.get(), serverId);
            messageService.updateStatusMessage(messageChannel, statusMessage.getIdLong(), newStatus);
            if(throwable != null) {
                log.warn("Failed to unset role in server {}.", serverId, throwable);
            }
            returningFuture.complete(null);
        });
        return returningFuture;
    }

    private MessageToSend getUserSyncStatusUpdateModel(Integer current, Integer total, Long serverId) {
        UserSyncStatusModel statusModel = UserSyncStatusModel
                .builder()
                .currentCount(current)
                .totalUserCount(total)
                .build();
        return templateService.renderEmbedTemplate("user_sync_status_message", statusModel, serverId);
    }


    @Override
    public AExperienceRole calculateRole(List<AExperienceRole> roles, Integer currentLevel) {
        if(roles == null || roles.isEmpty()) {
            return null;
        }
        log.debug("Calculating role for level {} in server {}. Using {} roles in our config.", currentLevel, roles.get(0).getServer().getId(), roles.size());
        AExperienceRole lastRole = null;
        for (AExperienceRole experienceRole : roles) {
            if(currentLevel >= experienceRole.getLevel().getLevel()) {
                lastRole = experienceRole;
            } else {
                return lastRole;
            }
        }
        return lastRole;
    }

    @Override
    public AExperienceLevel getLevelOfNextRole(AExperienceLevel startLevel, AServer server) {
        log.debug("Calculating level of next role for level {} in server {}.", startLevel.getLevel(), server.getId());
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(server);
        roles = roles.stream().filter(role -> role.getLevel().getLevel() > startLevel.getLevel()).collect(Collectors.toList());
        roles.sort(Comparator.comparing(role -> role.getLevel().getLevel()));
        AExperienceRole aExperienceRole = roles.stream().findFirst().orElse(null);
        return aExperienceRole != null ? aExperienceRole.getLevel() : AExperienceLevel.builder().level(200).build();
    }

    @Override
    public List<LevelRole> loadLevelRoleConfigForServer(AServer server) {
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(server);
        List<LevelRole> levelRoles = new ArrayList<>();
        roles.forEach(aExperienceRole -> {
            Role role = roleService.getRoleFromGuild(aExperienceRole.getRole());
            LevelRole levelRole = LevelRole
                    .builder()
                    .role(role)
                    .roleId(aExperienceRole.getId())
                    .level(aExperienceRole.getLevel().getLevel())
                    .build();
            levelRoles.add(levelRole);
        });
        return levelRoles;
    }

}

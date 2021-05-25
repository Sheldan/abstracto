package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.experience.model.RoleCalculationResult;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.template.LevelRole;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ExperienceRoleServiceBean implements ExperienceRoleService {

    @Autowired
    private ExperienceRoleManagementService experienceRoleManagementService;

    @Autowired
    private ExperienceLevelManagementService experienceLevelService;

    @Autowired
    private AUserExperienceService userExperienceService;

    @Autowired
    private ExperienceRoleServiceBean self;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private RoleService roleService;

    /**
     * UnSets the current configuration for the passed level, and sets the {@link ARole} to be used for this level
     * in the given {@link AServer}
     * @param role The {@link ARole} to set the level to
     * @param level The level the {@link ARole} should be awarded at
     */
    @Override
    public CompletableFuture<Void> setRoleToLevel(Role role, Integer level, Long channelId) {
        Long roleId = role.getIdLong();
        ARole aRoleToSet = roleManagementService.findRole(roleId);
        List<AExperienceRole> experienceRoles = getExperienceRolesAtLevel(level, aRoleToSet.getServer());
        List<ARole> rolesToUnset = experienceRoles.stream().map(AExperienceRole::getRole).collect(Collectors.toList());
        if(rolesToUnset.size() == 1 && rolesToUnset.contains(aRoleToSet)) {
            return CompletableFuture.completedFuture(null);
        }
        if(!rolesToUnset.contains(aRoleToSet)) {
            rolesToUnset.add(aRoleToSet);
        }
        AExperienceLevel experienceLevel;
        if(!experienceRoles.isEmpty()) {
            experienceLevel = experienceRoles.get(0).getLevel();
        } else {
            experienceLevel = experienceLevelService.getLevel(level);
        }
        AExperienceRole newExperienceRole = experienceRoleManagementService.setLevelToRole(experienceLevel, aRoleToSet);
        Long newlyCreatedExperienceRoleId = newExperienceRole.getId();
        CompletableFuture<Void> future = new CompletableFuture<>();
        unsetRoles(rolesToUnset, channelId, newExperienceRole).thenAccept(aVoid ->
            self.unsetRoleInDb(level, roleId)
        ).thenAccept(unused -> future.complete(null)).exceptionally(throwable -> {
            self.deleteExperienceRoleViaId(newlyCreatedExperienceRoleId);
            future.completeExceptionally(throwable);
            return null;
        });

        return future;
    }

    @Transactional
     public void deleteExperienceRoleViaId(Long newlyCreatedExperienceRoleId) {
        AExperienceRole reLoadedRole = experienceRoleManagementService.getExperienceRoleById(newlyCreatedExperienceRoleId);
        experienceRoleManagementService.unsetRole(reLoadedRole);
    }

    /**
     * Removes all previous defined {@link AExperienceRole experienceRoles} from the given level and sets the {@link ARole}
     * (defined by its ID) to the level.
     * @param level The level which the {@link ARole role} should be set to
     * @param roleId The ID of the {@link Role} which should have its level set
     */
    @Transactional
    public void unsetRoleInDb(Integer level, Long roleId) {
        log.info("Unsetting role {} from level {}.", roleId, level);
        AExperienceLevel experienceLevel = experienceLevelService.getLevelOptional(level).orElseThrow(() -> new IllegalArgumentException(String.format("Could not find level %s", level)));
        ARole loadedRole = roleManagementService.findRole(roleId);
        experienceRoleManagementService.removeAllRoleAssignmentsForLevelInServerExceptRole(experienceLevel, loadedRole.getServer(), loadedRole);
        experienceRoleManagementService.setLevelToRole(experienceLevel, loadedRole);
    }

    /**
     * Deletes the {@link AExperienceRole} and recalculates the experience for all users which currently had the associated
     * {@link net.dv8tion.jda.api.entities.Role}.
     * @param role The {@link ARole} to remove from the {@link dev.sheldan.abstracto.experience.model.database.AExperienceRole}
     *             configuration
     */
    @Override
    public CompletableFuture<Void> unsetRoles(ARole role, Long feedbackChannelId) {
        return unsetRoles(Arrays.asList(role), feedbackChannelId);
    }

    @Override
    public List<AExperienceRole> getExperienceRolesAtLevel(Integer level, AServer server) {
        AExperienceLevel levelObj = experienceLevelService.getLevel(level);
        return experienceRoleManagementService.getExperienceRolesAtLevelInServer(levelObj, server);
    }

    @Override
    public CompletableFuture<Void> unsetRoles(List<ARole> rolesToUnset, Long feedbackChannelId) {
        return unsetRoles(rolesToUnset, feedbackChannelId, null);
    }

    @Override
    public CompletableFuture<Void> unsetRoles(List<ARole> rolesToUnset, Long feedbackChannelId, AExperienceRole toAdd) {
        if(rolesToUnset.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        AServer server = rolesToUnset.get(0).getServer();
        AChannel channel = channelManagementService.loadChannel(feedbackChannelId);
        List<AExperienceRole> experienceRolesNecessaryToRemove = new ArrayList<>();
        List<AUserExperience> usersToUpdate = new ArrayList<>();
        rolesToUnset.forEach(role -> {
            Optional<AExperienceRole> roleInServerOptional = experienceRoleManagementService.getRoleInServerOptional(role);
            if(roleInServerOptional.isPresent()) {
                AExperienceRole experienceRole = roleInServerOptional.get();
                experienceRolesNecessaryToRemove.add(experienceRole);
                usersToUpdate.addAll(experienceRole.getUsers());
            } else {
                log.info("Experience role {} is not defined in server {} - skipping unset.", role.getId(), server.getId());
            }
        });
        log.info("Recalculating the roles for {} users, because their current role was removed from experience tracking.", usersToUpdate.size());
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(server);
        roles.removeIf(role1 -> experienceRolesNecessaryToRemove.stream().anyMatch(aExperienceRole -> aExperienceRole.getId().equals(role1.getId())));
        if(toAdd != null) {
            roles.add(toAdd);
        }
        roles.sort(Comparator.comparing(innerRole -> innerRole.getLevel().getLevel()));
        List<Long> roleIds = experienceRolesNecessaryToRemove.stream().map(AExperienceRole::getId).collect(Collectors.toList());
        if(toAdd != null) {
            roleIds.removeIf(aLong -> aLong.equals(toAdd.getRole().getId()));
        }
        CompletableFutureList<RoleCalculationResult> calculationResults = userExperienceService.executeActionOnUserExperiencesWithFeedBack(usersToUpdate, channel,
                (AUserExperience ex) -> userExperienceService.updateUserRole(ex, roles, ex.getLevelOrDefault()));
        return calculationResults.getMainFuture().thenAccept(aVoid -> self.persistData(calculationResults, roleIds));
    }

    /**
     * Stores the changed experience roles for all of the {@link AUserExperience userExperiences} which are referenced in the list of
     * {@link RoleCalculationResult results}. This is only executed after a role is being "unset", which means, we also
     * have to remove the existing {@link AExperienceRole experienceRole}
     * @param results A list of {@link CompletableFuture futures} which each contain a {@link RoleCalculationResult result}, for the members who got
     *                their {@link AExperienceRole experienceRole} removed
     * @param roleIds The IDs of the {@link AExperienceRole experienceRoles} which were removed from the experience roles
     */
    @Transactional
    public void persistData(CompletableFutureList<RoleCalculationResult> results, List<Long> roleIds) {
        log.info("Persisting {} role calculation results.", results.getFutures().size());
        roleIds.forEach(roleId -> {
            log.info("Deleting experience role {}.", roleId);
            deleteExperienceRoleViaId(roleId);
        });
        userExperienceService.syncRolesInStorage(results.getObjects());
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

package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.experience.models.RoleCalculationResult;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

    /**
     * UnSets the current configuration for the passed level, and sets the {@link ARole} to be used for this level
     * in the given {@link AServer}
     * @param role The {@link ARole} to set the level to
     * @param level The level the {@link ARole} should be awarded at
     */
    @Override
    public CompletableFuture<Void> setRoleToLevel(Role role, Integer level, AChannel feedbackChannel) {
        Long roleId = role.getIdLong();
        ARole aRole = roleManagementService.findRole(roleId);
        return unsetRole(aRole, feedbackChannel).thenAccept(aVoid ->
            self.unsetRoleInDb(level, roleId)
        );
    }

    @Transactional
    public void unsetRoleInDb(Integer level, Long roleId) {
        log.info("Unsetting role {} from level {}.", roleId, level);
        AExperienceLevel experienceLevel = experienceLevelService.getLevel(level).orElseThrow(() -> new IllegalArgumentException(String.format("Could not find level %s", level)));
        ARole loadedRole = roleManagementService.findRole(roleId);
        experienceRoleManagementService.removeAllRoleAssignmentsForLevelInServer(experienceLevel, loadedRole.getServer());
        experienceRoleManagementService.setLevelToRole(experienceLevel, loadedRole);
    }

    /**
     * Deletes the {@link AExperienceRole} and recalculates the experience for all users which currently had the associated
     * {@link net.dv8tion.jda.api.entities.Role}.
     * @param role The {@link ARole} to remove from the {@link dev.sheldan.abstracto.experience.models.database.AExperienceRole}
     *             configuration
     */
    @Override
    public CompletableFuture<Void> unsetRole(ARole role, AChannel feedbackChannel) {
        Optional<AExperienceRole> roleInServerOptional = experienceRoleManagementService.getRoleInServerOptional(role);
        if(roleInServerOptional.isPresent()) {
            AExperienceRole roleInServer = roleInServerOptional.get();
            if(!roleInServer.getUsers().isEmpty()) {
                log.info("Recalculating the roles for {} users, because their current role was removed from experience tracking.", roleInServer.getUsers().size());
                List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(role.getServer());
                roles.removeIf(role1 -> role1.getId().equals(roleInServer.getId()));
                Long roleId = role.getId();
                CompletableFutureList<RoleCalculationResult> calculationResults = userExperienceService.executeActionOnUserExperiencesWithFeedBack(roleInServer.getUsers(), feedbackChannel,
                        (AUserExperience ex) -> userExperienceService.updateUserRole(ex, roles, ex.getLevelOrDefault()));
                return calculationResults.getMainFuture().thenAccept(aVoid ->
                        self.persistData(calculationResults, roleId)
                );
            } else {
                log.info("Roles does not have any active users, no need to remove them.");
                experienceRoleManagementService.unsetRole(roleInServer);
                return CompletableFuture.completedFuture(null);
            }
        } else {
            log.info("Experience role is not define in server - skipping unset.");
            return CompletableFuture.completedFuture(null);
        }
    }

    @Transactional
    public void persistData(CompletableFutureList<RoleCalculationResult> results, Long roleId) {
        log.info("Persisting {} role calculation results after changing the role {}.", results.getFutures().size(), roleId);
        AExperienceRole roleInServer = experienceRoleManagementService.getRoleInServer(roleId);
        experienceRoleManagementService.unsetRole(roleInServer);
        userExperienceService.syncRolesInStorage(results.getObjects());
    }

    /**
     * Finds the best {@link AExperienceRole} for the level of the passed {@link AUserExperience}, returns null if the passed
     * roles are empty/null
     * @param roles The role configuration to be used when calculating the appropriate {@link AExperienceRole}
     * @param currentLevel
     * @return The best fitting {@link AExperienceRole} according to the level of the {@link AUserExperience}
     */
    @Override
    public AExperienceRole calculateRole(List<AExperienceRole> roles, Integer currentLevel) {
        if(roles == null || roles.isEmpty()) {
            return null;
        }
        log.trace("Calculating role for level {} in server {}. Using {} roles in our config.", currentLevel, roles.get(0).getRoleServer().getId(), roles.size());
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
        log.trace("Calculating level of next role for level {} in server {}.", startLevel.getLevel(), server.getId());
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(server);
        roles = roles.stream().filter(role -> role.getLevel().getLevel() > startLevel.getLevel()).collect(Collectors.toList());
        roles.sort(Comparator.comparing(role -> role.getLevel().getLevel()));
        AExperienceRole aExperienceRole = roles.stream().findFirst().orElse(null);
        return aExperienceRole != null ? aExperienceRole.getLevel() : AExperienceLevel.builder().level(200).build();
    }

}

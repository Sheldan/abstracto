package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
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

    /**
     * UnSets the current configuration for the passed level, and sets the {@link ARole} to be used for this level
     * in the given {@link AServer}
     * @param role The {@link ARole} to set the level to
     * @param level The level the {@link ARole} should be awarded at
     */
    @Override
    public void setRoleToLevel(ARole role, Integer level, AChannel feedbackChannel) {
        AExperienceLevel experienceLevel = experienceLevelService.getLevel(level).orElseThrow(() -> new IllegalArgumentException(String.format("Could not find level %s", level)));
        unsetRole(role, feedbackChannel);
        experienceRoleManagementService.removeAllRoleAssignmentsForLevelInServer(experienceLevel, role.getServer());
        experienceRoleManagementService.setLevelToRole(experienceLevel, role);
    }

    /**
     * Deletes the {@link AExperienceRole} and recalculates the experience for all users which currently had the associated
     * {@link net.dv8tion.jda.api.entities.Role}.
     * @param role The {@link ARole} to remove from the {@link dev.sheldan.abstracto.experience.models.database.AExperienceRole}
     *             configuration
     */
    @Override
    public void unsetRole(ARole role, AChannel feedbackChannel) {
        AExperienceRole roleInServer = experienceRoleManagementService.getRoleInServer(role);
        if(roleInServer != null) {
            if(!roleInServer.getUsers().isEmpty()) {
                log.info("Recalculating the roles for {} users, because their current role was removed from experience tracking.", roleInServer.getUsers().size());
                List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(role.getServer());
                roles.removeIf(role1 -> role1.getId().equals(roleInServer.getId()));

                userExperienceService.executeActionOnUserExperiencesWithFeedBack(roleInServer.getUsers(), feedbackChannel,
                        (AUserExperience ex) -> userExperienceService.updateUserRole(ex, roles));
            }
            experienceRoleManagementService.unsetRole(roleInServer);
        }
    }

    /**
     * Finds the best {@link AExperienceRole} for the level of the passed {@link AUserExperience}
     * @param userExperience The {@link AUserExperience} containing the level to calculate the {@link AExperienceRole}
     * @param roles The role configuration to be used when calculating the appropriate {@link AExperienceRole}
     * @return The best fitting {@link AExperienceRole} according to the level of the {@link AUserExperience}
     */
    @Override
    public AExperienceRole calculateRole(AUserExperience userExperience, List<AExperienceRole> roles) {
        if(roles.isEmpty()) {
            return null;
        }
        AExperienceRole lastRole = null;
        for (AExperienceRole experienceRole : roles) {
            if(userExperience.getCurrentLevel().getLevel() >= experienceRole.getLevel().getLevel()) {
                lastRole = experienceRole;
            } else {
                return lastRole;
            }
        }
        return lastRole;
    }

    @Override
    public AExperienceLevel getLevelOfNextRole(AExperienceLevel startLevel, AServer server) {
        List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRolesForServer(server);
        roles = roles.stream().filter(role -> role.getLevel().getLevel() > startLevel.getLevel()).collect(Collectors.toList());
        roles.sort(Comparator.comparing(role -> role.getLevel().getLevel()));
        AExperienceRole aExperienceRole = roles.stream().findFirst().orElse(null);
        return aExperienceRole != null ? aExperienceRole.getLevel() : AExperienceLevel.builder().level(200).build();
    }

}

package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ExperienceRoleServiceBean implements ExperienceRoleService {

    @Autowired
    private ExperienceRoleManagementService experienceRoleManagementService;

    @Autowired
    private ExperienceLevelManagementService experienceLevelService;

    @Autowired
    private ExperienceTrackerService experienceTrackerService;

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Override
    public void setRoleToLevel(ARole role, Integer level, AServer server) {
        AExperienceLevel experienceLevel = experienceLevelService.getLevel(level);
        experienceRoleManagementService.unSetLevelInServer(experienceLevel, server);
        experienceRoleManagementService.setLevelToRole(experienceLevel, role, server);
    }

    @Override
    public void unsetRole(ARole role, AServer server) {
        AExperienceRole roleInServer = experienceRoleManagementService.getRoleInServer(role, server);
        if(roleInServer.getUsers().size() > 0) {
            log.info("Recalculating the roles for {} users, because their current role was removed from experience tracking.", roleInServer.getUsers().size());
            roleInServer.getUsers().forEach(userExperience -> {
                List<AExperienceRole> roles = experienceRoleManagementService.getExperienceRoleForServer(server);
                roles.removeIf(role1 -> role1.getId().equals(roleInServer.getId()));
                experienceTrackerService.handleExperienceRoleForUser(userExperience, roles);
            });
        }
        experienceRoleManagementService.unsetRole(roleInServer);
    }

}

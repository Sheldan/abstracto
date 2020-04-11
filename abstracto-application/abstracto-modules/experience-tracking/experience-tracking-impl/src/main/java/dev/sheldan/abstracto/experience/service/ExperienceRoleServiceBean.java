package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExperienceRoleServiceBean implements ExperienceRoleService {

    @Autowired
    private ExperienceRoleManagementService experienceRoleManagementService;

    @Autowired
    private ExperienceLevelManagementService experienceLevelService;

    @Override
    public void setRoleToLevel(ARole role, Integer level, AServer server) {
        AExperienceLevel experienceLevel = experienceLevelService.getLevel(level);
        experienceRoleManagementService.unSetLevelInServer(experienceLevel, server);
        experienceRoleManagementService.setLevelToRole(experienceLevel, role, server);
    }

}

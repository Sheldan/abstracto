package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;

import java.util.List;

public interface ExperienceRoleManagementService {
    void setLevelToRole(AExperienceLevel level, ARole role, AServer server);
    void unSetLevelInServer(AExperienceLevel level, AServer server);
    List<AExperienceRole> getExperienceRoleForServer(AServer server);
}

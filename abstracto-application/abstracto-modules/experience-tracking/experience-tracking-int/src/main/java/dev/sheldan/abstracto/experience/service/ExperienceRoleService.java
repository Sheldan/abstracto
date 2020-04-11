package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;

public interface ExperienceRoleService {
    void setRoleToLevel(ARole role, Integer level, AServer server);
}

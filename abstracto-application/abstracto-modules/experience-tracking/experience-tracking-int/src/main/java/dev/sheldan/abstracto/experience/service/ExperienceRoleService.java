package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;

public interface ExperienceRoleService {
    void setRoleToLevel(ARole role, Integer level, AServer server);
    void unsetRole(ARole role, AServer server);
}

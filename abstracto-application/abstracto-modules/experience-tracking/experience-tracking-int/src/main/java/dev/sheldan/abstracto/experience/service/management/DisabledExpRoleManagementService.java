package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.ADisabledExpRole;

import java.util.List;

public interface DisabledExpRoleManagementService {
    ADisabledExpRole setRoleToBeDisabledForExp(ARole role);
    void removeRoleToBeDisabledForExp(ARole role);
    List<ADisabledExpRole> getDisabledRolesForServer(AServer server);
    boolean isExperienceDisabledForRole(ARole role);
}

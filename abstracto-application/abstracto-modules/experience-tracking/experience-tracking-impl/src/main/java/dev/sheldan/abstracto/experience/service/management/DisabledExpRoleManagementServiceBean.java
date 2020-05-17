package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.ADisabledExpRole;
import dev.sheldan.abstracto.experience.repository.DisabledExpRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DisabledExpRoleManagementServiceBean implements DisabledExpRoleManagementService {

    @Autowired
    private DisabledExpRoleRepository disabledExpRoleRepository;

    @Override
    public ADisabledExpRole setRoleToBeDisabledForExp(ARole role) {
        ADisabledExpRole newRole = ADisabledExpRole
                .builder()
                .role(role)
                .build();

        disabledExpRoleRepository.save(newRole);
        return newRole;
    }

    @Override
    public void removeRoleToBeDisabledForExp(ARole role) {
        disabledExpRoleRepository.deleteByRole(role);
    }

    @Override
    public List<ADisabledExpRole> getDisabledRolesForServer(AServer server) {
        return disabledExpRoleRepository.getByRole_Server(server);
    }

    @Override
    public boolean isExperienceDisabledForRole(ARole role) {
        return disabledExpRoleRepository.existsByRole(role);
    }


}

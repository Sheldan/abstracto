package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.ADisabledExpRole;
import dev.sheldan.abstracto.experience.repository.DisabledExpRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DisabledExpRoleManagementServiceBean implements DisabledExpRoleManagementService {

    @Autowired
    private DisabledExpRoleRepository disabledExpRoleRepository;

    @Override
    public ADisabledExpRole setRoleToBeDisabledForExp(ARole role) {
        ADisabledExpRole newRole = ADisabledExpRole
                .builder()
                .role(role)
                .build();
        log.info("Adding disabled exp role {} for server {}.", role.getId(), role.getServer().getId());
        return disabledExpRoleRepository.save(newRole);
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

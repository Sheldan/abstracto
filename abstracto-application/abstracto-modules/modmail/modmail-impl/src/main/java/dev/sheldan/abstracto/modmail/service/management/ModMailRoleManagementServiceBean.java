package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailRole;
import dev.sheldan.abstracto.modmail.repository.ModMailRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ModMailRoleManagementServiceBean implements ModMailRoleManagementService {

    @Autowired
    private ModMailRoleRepository modMailRoleRepository;

    @Override
    public void addRoleToModMailRoles(ARole role) {
        ModMailRole roleToAdd = ModMailRole
                .builder()
                .role(role)
                .server(role.getServer())
                .build();
        log.info("Adding role {} in server {} to modmail roles.", role.getId(), role.getServer());
        modMailRoleRepository.save(roleToAdd);
    }

    @Override
    public void removeRoleFromModMailRoles(ARole role) {
        modMailRoleRepository.deleteByServerAndRole(role.getServer(), role);
    }

    @Override
    public List<ModMailRole> getRolesForServer(AServer server) {
        return modMailRoleRepository.findByServer(server);
    }

    @Override
    public boolean isRoleAlreadyAssigned(ARole role) {
        return modMailRoleRepository.existsByServerAndRole(role.getServer(), role);
    }
}

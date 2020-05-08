package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailRole;
import dev.sheldan.abstracto.modmail.repository.ModMailRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModMailRoleManagementServiceBean implements ModMailRoleManagementService {

    @Autowired
    private ModMailRoleRepository modMailRoleRepository;

    @Override
    public void addRoleToModMailRoles(ARole role, AServer server) {
        ModMailRole roleToAdd = ModMailRole
                .builder()
                .role(role)
                .server(server)
                .build();
        modMailRoleRepository.save(roleToAdd);
    }

    @Override
    public void removeRoleFromModMailRoles(ARole role, AServer server) {
        modMailRoleRepository.deleteByServerAndRole(server, role);
    }

    @Override
    public List<ModMailRole> getRolesForServer(AServer server) {
        return modMailRoleRepository.findByServer(server);
    }

    @Override
    public boolean isRoleAlreadyAssigned(ARole role, AServer server) {
        return modMailRoleRepository.existsByServerAndRole(server, role);
    }
}

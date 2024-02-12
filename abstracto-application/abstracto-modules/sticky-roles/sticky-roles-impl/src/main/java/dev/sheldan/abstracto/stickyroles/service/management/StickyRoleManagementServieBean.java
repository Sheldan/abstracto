package dev.sheldan.abstracto.stickyroles.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.stickyroles.model.database.StickyRole;
import dev.sheldan.abstracto.stickyroles.repository.StickyRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class StickyRoleManagementServieBean implements StickyRoleManagementService {

    @Autowired
    private StickyRoleRepository stickyRoleRepository;

    @Autowired
    private RoleManagementService roleManagementService;


    @Override
    public StickyRole getOrCreateStickyRole(Long roleId) {
        Optional<StickyRole> existingRole = stickyRoleRepository.findById(roleId);
        return existingRole.orElseGet(() -> createStickyRole(roleId));
    }

    @Override
    public StickyRole createStickyRole(Long roleId) {
        ARole aRole = roleManagementService.findRole(roleId);
        StickyRole role = StickyRole
                .builder()
                .id(roleId)
                .sticky(StickyRoleManagementService.DEFAULT_STICKINESS)
                .role(aRole)
                .server(aRole.getServer())
                .build();
        return stickyRoleRepository.save(role);
    }

    @Override
    public List<StickyRole> createStickyRoles(List<Long> roleIds) {
        return roleIds
                .stream()
                .map(this::createStickyRole)
                .toList();
    }

    @Override
    public List<StickyRole> getRoles(List<Long> roleIds) {
        return stickyRoleRepository.findAllById(roleIds);
    }

    @Override
    public List<StickyRole> getStickyRolesForServer(AServer server) {
        return stickyRoleRepository.findStickyRoleByServer(server);
    }
}

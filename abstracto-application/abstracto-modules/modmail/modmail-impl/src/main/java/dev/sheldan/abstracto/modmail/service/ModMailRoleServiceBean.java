package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.modmail.config.ModMailFeatures;
import dev.sheldan.abstracto.modmail.service.management.ModMailRoleManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ModMailRoleServiceBean implements ModMailRoleService {

    @Autowired
    private ModMailRoleManagementService modMailRoleManagementService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Override
    public void addRoleToModMailRoles(ARole role) {
        log.info("Adding role {} to modmail roles in server {}.", role.getId(), role.getServer().getId());
        if(!modMailRoleManagementService.isRoleAlreadyAssigned(role)) {
            modMailRoleManagementService.addRoleToModMailRoles(role);
        }
        commandService.allowFeatureForRole(ModMailFeatures.MOD_MAIL, role);
    }

    @Override
    public void removeRoleFromModMailRoles(ARole role) {
        log.info("Remove role {} from modmail roles in server {}.", role.getId(), role.getServer().getId());
        modMailRoleManagementService.removeRoleFromModMailRoles(role);
        commandService.disAllowFeatureForRole(ModMailFeatures.MOD_MAIL, role);
    }
}

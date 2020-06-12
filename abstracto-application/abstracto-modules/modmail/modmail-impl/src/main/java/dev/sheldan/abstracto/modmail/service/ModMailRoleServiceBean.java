package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.modmail.config.ModMailFeatures;
import dev.sheldan.abstracto.modmail.service.management.ModMailRoleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModMailRoleServiceBean implements ModMailRoleService {

    @Autowired
    private ModMailRoleManagementService modMailRoleManagementService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Override
    public void addRoleToModMailRoles(ARole role) {
        if(!modMailRoleManagementService.isRoleAlreadyAssigned(role, role.getServer())) {
            modMailRoleManagementService.addRoleToModMailRoles(role, role.getServer());
        }
        commandService.allowFeatureForRole(ModMailFeatures.MOD_MAIL, role);
    }

    @Override
    public void removeRoleFromModMailRoles(ARole role) {
        modMailRoleManagementService.removeRoleFromModMailRoles(role, role.getServer());
        commandService.disAllowFeatureForRole(ModMailFeatures.MOD_MAIL, role);
    }
}

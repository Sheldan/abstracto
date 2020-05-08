package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
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
    public void addRoleToModMailRoles(ARole role, AServer server) {
        if(!modMailRoleManagementService.isRoleAlreadyAssigned(role, server)) {
            modMailRoleManagementService.addRoleToModMailRoles(role, server);
        }
        commandService.allowFeatureForRole(ModMailFeatures.MODMAIL, role);
    }

    @Override
    public void removeRoleFromModMailRoles(ARole role, AServer server) {
        modMailRoleManagementService.removeRoleFromModMailRoles(role, server);
        commandService.disAllowFeatureForRole(ModMailFeatures.MODMAIL, role);
    }
}

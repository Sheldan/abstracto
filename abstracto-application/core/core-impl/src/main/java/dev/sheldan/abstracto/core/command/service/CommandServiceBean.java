package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.models.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.models.database.AModule;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.command.service.management.ModuleManagementService;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandServiceBean implements CommandService {

    @Autowired
    private ModuleManagementService moduleManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Override
    public ACommand createCommand(String name, String moduleName, FeatureEnum featureEnum) {
        AModule module = moduleManagementService.getOrCreate(moduleName);
        AFeature feature = featureManagementService.getFeature(featureEnum.getKey());
        return commandManagementService.createCommand(name, module, feature);
    }

    @Override
    public Boolean doesCommandExist(String name) {
        return commandManagementService.doesCommandExist(name);
    }

    @Override
    public void allowCommandForRole(ACommand aCommand, ARole role) {
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, role.getServer());
        if(!commandForServer.getAllowedRoles().contains(role)) {
            commandForServer.getAllowedRoles().add(role);
        }
        commandForServer.setRestricted(true);
    }

    @Override
    public void makeRoleImmuneForCommand(ACommand aCommand, ARole role) {
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, role.getServer());
        if(!commandForServer.getImmuneRoles().contains(role)) {
            commandForServer.getImmuneRoles().add(role);
        }
    }

    @Override
    public void makeRoleAffectedByCommand(ACommand aCommand, ARole role) {
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, role.getServer());
        commandForServer.getImmuneRoles().remove(role);
    }

    @Override
    public void restrictCommand(ACommand aCommand, AServer server) {
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, server);
        commandForServer.setRestricted(true);
    }

    @Override
    public void unRestrictCommand(ACommand aCommand, AServer server) {
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, server);
        commandForServer.setRestricted(false);
    }

    @Override
    public void disAllowCommandForRole(ACommand aCommand, ARole role) {
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, role.getServer());
        commandForServer.setRestricted(true);
        commandForServer.getAllowedRoles().remove(role);
    }


}

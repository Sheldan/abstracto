package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.condition.ConditionalCommand;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
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
        if(featureEnum == null) {
            log.warn("Command {} in module {} has no feature.", name, moduleName);
            return null;
        }
        AFeature feature = featureManagementService.getFeature(featureEnum.getKey());
        return commandManagementService.createCommand(name, module, feature);
    }

    @Override
    public boolean doesCommandExist(String name) {
        return commandManagementService.doesCommandExist(name);
    }

    @Override
    public void allowCommandForRole(ACommand aCommand, ARole role) {
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, role.getServer());
        if(commandForServer.getAllowedRoles().stream().noneMatch(role1 -> role1.getId().equals(role.getId()))) {
            commandForServer.getAllowedRoles().add(role);
        }
        commandForServer.setRestricted(true);
    }

    @Override
    public void allowFeatureForRole(FeatureEnum featureEnum, ARole role) {
        AFeature feature = featureManagementService.getFeature(featureEnum.getKey());
        feature.getCommands().forEach(command -> this.allowCommandForRole(command, role));
    }

    @Override
    public void makeRoleImmuneForCommand(ACommand aCommand, ARole role) {
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, role.getServer());
        if(commandForServer.getImmuneRoles().stream().noneMatch(role1 -> role1.getId().equals(role.getId()))) {
            commandForServer.getImmuneRoles().add(role);
        }
    }

    @Override
    public void makeRoleAffectedByCommand(ACommand aCommand, ARole role) {
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, role.getServer());
        commandForServer.getImmuneRoles().removeIf(role1 -> role1.getId().equals(role.getId()));
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
        commandForServer.getAllowedRoles().removeIf(role1 -> role1.getId().equals(role.getId()));
    }

    @Override
    public void disAllowFeatureForRole(FeatureEnum featureEnum, ARole role) {
        AFeature feature = featureManagementService.getFeature(featureEnum.getKey());
        feature.getCommands().forEach(command -> this.disAllowCommandForRole(command, role));
    }

    public ConditionResult isCommandExecutable(Command command, CommandContext commandContext) {
        if(command instanceof ConditionalCommand) {
            ConditionalCommand castedCommand = (ConditionalCommand) command;
            return checkConditions(commandContext, command, castedCommand.getConditions());
        } else {
            return ConditionResult.builder().result(true).build();
        }
    }

    private ConditionResult checkConditions(CommandContext commandContext, Command command, List<CommandCondition> conditions) {
        if(conditions != null) {
            for (CommandCondition condition : conditions) {
                ConditionResult conditionResult = condition.shouldExecute(commandContext, command);
                if(!conditionResult.isResult()) {
                    return conditionResult;
                }
            }
        }
        return ConditionResult.builder().result(true).build();
    }


}

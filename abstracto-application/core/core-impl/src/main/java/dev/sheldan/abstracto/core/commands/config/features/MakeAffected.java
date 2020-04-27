package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleInterface;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MakeAffected extends AbstractConditionableCommand {

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String name = (String) commandContext.getParameters().getParameters().get(0);
        Long roleId = (Long) commandContext.getParameters().getParameters().get(1);
        ARole role = roleManagementService.findRole(roleId);
        if(featureManagementService.featureExists(name)) {
            AFeature feature = featureManagementService.getFeature(name);
            feature.getCommands().forEach(command -> {
                commandService.makeRoleAffectedByCommand(command, role);
            });
        } else if(commandManagementService.doesCommandExist(name)) {
            ACommand command = commandManagementService.findCommandByName(name);
            commandService.makeRoleAffectedByCommand(command, role);
        } else {
            return CommandResult.fromError("No Feature/Command with that name");
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter.builder().name("feature|commandName").type(String.class).description("The command/feature name to make the role affected by.").build();
        Parameter role = Parameter.builder().name("roleId").type(Long.class).description("The roleId to make affected.").build();
        List<Parameter> parameters = Arrays.asList(featureName, role);
        return CommandConfiguration.builder()
                .name("makeAffected")
                .module(ConfigModuleInterface.CONFIG)
                .parameters(parameters)
                .description("Makes a role vulnerable to be affected by certain commands.")
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}

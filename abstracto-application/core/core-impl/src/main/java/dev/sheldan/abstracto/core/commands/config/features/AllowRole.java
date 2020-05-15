package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleInterface;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AllowRole extends AbstractConditionableCommand {

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private TemplateService templateService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String name = (String) commandContext.getParameters().getParameters().get(0);
        ARole role = (ARole) commandContext.getParameters().getParameters().get(1);
        if(featureManagementService.featureExists(name)) {
            FeatureEnum featureEnum = featureFlagService.getFeatureEnum(name);
            commandService.allowFeatureForRole(featureEnum, role);
        } else if(commandManagementService.doesCommandExist(name)) {
            ACommand command = commandManagementService.findCommandByName(name);
            commandService.allowCommandForRole(command, role);
        } else {
            return CommandResult.fromError(templateService.renderTemplate("no_feature_command_found", new Object()));
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter.builder().name("component").type(String.class).templated(true).build();
        Parameter role = Parameter.builder().name("role").type(ARole.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(featureName, role);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("allowRole")
                .module(ConfigModuleInterface.CONFIG)
                .parameters(parameters)
                .templated(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}

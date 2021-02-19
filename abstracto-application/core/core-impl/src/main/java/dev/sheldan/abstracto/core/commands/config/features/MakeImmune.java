package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.CommandServiceBean;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleInterface;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MakeImmune extends AbstractConditionableCommand {

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private TemplateService templateService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String name = (String) commandContext.getParameters().getParameters().get(0);
        ARole role = (ARole) commandContext.getParameters().getParameters().get(1);
        ARole actualRole = roleManagementService.findRole(role.getId());
        if(featureManagementService.featureExists(name)) {
            AFeature feature = featureManagementService.getFeature(name);
            feature.getCommands().forEach(command ->
                commandService.makeRoleImmuneForCommand(command, actualRole)
            );
        } else if(commandManagementService.doesCommandExist(name)) {
            ACommand command = commandManagementService.findCommandByName(name);
            commandService.makeRoleImmuneForCommand(command, actualRole);
        } else {
            // TODO refactor to use exception
            return CommandResult.fromError(templateService.renderTemplate(CommandServiceBean.NO_FEATURE_COMMAND_FOUND_EXCEPTION_TEMPLATE,
                    new Object(), commandContext.getGuild().getIdLong()));
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter.builder().name("component").type(String.class).templated(true).build();
        Parameter role = Parameter.builder().name("role").type(ARole.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(featureName, role);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("makeImmune")
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

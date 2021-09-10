package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.model.CommandServiceBean;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class Restrict extends AbstractConditionableCommand {

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

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String name = (String) commandContext.getParameters().getParameters().get(0);
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        if(featureManagementService.featureExists(name)) {
            AFeature feature = featureManagementService.getFeature(name);
            feature.getCommands().forEach(command ->
                commandService.restrictCommand(command, server)
            );
        } else if(commandManagementService.doesCommandExist(name)) {
            ACommand command = commandManagementService.findCommandByName(name);
            commandService.restrictCommand(command, server);
        } else {
            // TODO Refactor to use exception
            return CommandResult.fromError(templateService.renderTemplate(CommandServiceBean.NO_FEATURE_COMMAND_FOUND_EXCEPTION_TEMPLATE,
                    new Object(), commandContext.getGuild().getIdLong()));
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter.builder().name("component").type(String.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(featureName);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("restrict")
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .templated(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}

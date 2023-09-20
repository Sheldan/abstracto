package dev.sheldan.abstracto.core.commands.config.cooldown;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
public class CommandCoolDownServer extends AbstractConditionableCommand {

    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String commandName = (String) parameters.get(0);
        ACommand aCommand = commandManagementService.findCommandByName(commandName);
        Duration duration = (Duration) parameters.get(1);
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, commandContext.getGuild().getIdLong());
        commandForServer.setCoolDown(duration.getSeconds());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter commandName = Parameter
                .builder()
                .name("command")
                .templated(true)
                .type(String.class)
                .build();
        Parameter coolDownDuration = Parameter
                .builder()
                .name("duration")
                .templated(true)
                .type(Duration.class)
                .build();
        List<Parameter> parameters = Arrays.asList(commandName, coolDownDuration);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("commandCoolDownServer")
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .messageCommandOnly(true)
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

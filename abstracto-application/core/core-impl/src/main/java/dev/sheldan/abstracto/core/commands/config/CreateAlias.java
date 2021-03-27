package dev.sheldan.abstracto.core.commands.config;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.CommandInServerAliasService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CreateAlias extends AbstractConditionableCommand {

    @Autowired
    private CommandInServerAliasService aliasService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String commandName = (String) parameters.get(0);
        String aliasName = (String) parameters.get(1);
        aliasService.createAliasForCommandInServer(commandContext.getGuild().getIdLong(), commandName, aliasName);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter commandNameParameter = Parameter.builder().name("commandName").type(String.class).templated(true).build();
        Parameter aliasParameter = Parameter.builder().name("alias").type(String.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(commandNameParameter, aliasParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("createAlias")
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .templated(true)
                .supportsEmbedException(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}

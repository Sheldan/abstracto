package dev.sheldan.abstracto.core.interaction.slash;

import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class SlashCommandServiceBean implements SlashCommandService {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private SlashCommandServiceBean self;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Override
    public void convertCommandConfigToCommandData(CommandConfiguration commandConfiguration, List<Pair<List<CommandConfiguration>, SlashCommandData>> existingCommands, Long serverId) {
        boolean isTemplated = commandConfiguration.isTemplated();
        SlashCommandConfig slashConfig = commandConfiguration.getSlashCommandConfig();
        String description;
        String internalCommandName = commandConfiguration.getName();
        if(!isTemplated) {
            description = commandConfiguration.getDescription();
        } else {
            description = templateService.renderSimpleTemplate(internalCommandName + "_description");
        }
        String rootName = slashConfig.getSlashCompatibleRootName();
        String groupName = slashConfig.getSlashCompatibleGroupName();
        String commandName = slashConfig.getSlashCompatibleCommandName();
        Optional<SlashCommandData> existingRootCommand = existingCommands
                .stream()
                .filter(commandData -> commandData.getSecond().getName().equals(rootName))
                .map(Pair::getSecond)
                .findAny();
        SlashCommandData rootCommand = existingRootCommand.orElseGet(() -> Commands.slash(rootName, description));
        if(commandName != null) {
            SubcommandData slashCommand = new SubcommandData(commandName, description);
            if(groupName == null) {
                rootCommand.addSubcommands(slashCommand);
            } else {
                Optional<SubcommandGroupData> commandGroup = rootCommand
                        .getSubcommandGroups()
                        .stream()
                        .filter(subcommandGroupData -> subcommandGroupData.getName().equals(groupName))
                        .findAny();
                SubcommandGroupData groupData = commandGroup.orElseGet(() -> new SubcommandGroupData(groupName, description));
                groupData.addSubcommands(slashCommand);
                if(commandGroup.isEmpty()) {
                    rootCommand.addSubcommandGroups(groupData);
                }
            }
            List<OptionData> requiredParameters = getParameters(commandConfiguration, isTemplated, internalCommandName, serverId);
            slashCommand.addOptions(requiredParameters);
        } else {
            List<OptionData> requiredParameters = getParameters(commandConfiguration, isTemplated, internalCommandName, serverId);
            rootCommand.addOptions(requiredParameters);
        }
        if(!existingRootCommand.isPresent()) {
            Optional<Pair<List<CommandConfiguration>, SlashCommandData>> existingCommand = existingCommands
                    .stream()
                    .filter(listSlashCommandDataPair -> listSlashCommandDataPair.getSecond().equals(rootCommand))
                    .findAny();
            if(existingCommand.isPresent()) {
                existingCommand.get().getFirst().add(commandConfiguration);
            } else {
                existingCommands.add(Pair.of(new ArrayList<>(Arrays.asList(commandConfiguration)), rootCommand));
            }
        }
    }

    @Override
    public void convertCommandConfigToCommandData(CommandConfiguration commandConfiguration, List<Pair<List<CommandConfiguration>, SlashCommandData>> existingCommands) {
        convertCommandConfigToCommandData(commandConfiguration, existingCommands, null);
    }

    private List<OptionData> getParameters(CommandConfiguration commandConfiguration, boolean isTemplated, String internalCommandName, Long serverId) {
        List<OptionData> requiredParameters = new ArrayList<>();
        List<OptionData> optionalParameters = new ArrayList<>();
        commandConfiguration.getParameters().forEach(parameter -> {
            if(!shouldParameterBeCreated(parameter, serverId)) {
                return;
            }
            List<OptionType> types = slashCommandParameterService.getTypesFromParameter(parameter.getType());
            if(types.size() > 1) {
                if(parameter.isListParam()) {
                    for (int i = 0; i < parameter.getListSize(); i++) {
                        for (OptionType type : types) {
                            String parameterName = slashCommandParameterService.getFullQualifiedParameterName(parameter.getSlashCompatibleName(), type) + "_" + i;
                            String parameterDescription = isTemplated ? templateService.renderSimpleTemplate(internalCommandName + "_parameter_" + parameter.getName()) : parameter.getDescription();
                            OptionData optionData = new OptionData(type, parameterName, parameterDescription, false);
                            addChoices(optionData, parameter, internalCommandName, isTemplated);
                            optionalParameters.add(optionData);
                        }
                    }
                } else {
                    types.forEach(type -> {
                        String parameterName = slashCommandParameterService.getFullQualifiedParameterName(parameter.getSlashCompatibleName(), type);
                        String parameterDescription = isTemplated ? templateService.renderSimpleTemplate(internalCommandName + "_parameter_" + parameter.getName()) : parameter.getDescription();
                        OptionData optionData = new OptionData(type, parameterName, parameterDescription, false);
                        addChoices(optionData, parameter, internalCommandName, isTemplated);
                        optionalParameters.add(optionData);
                    });
                }
            } else {
                OptionType type = types.get(0);
                String parameterDescription = isTemplated ? templateService.renderSimpleTemplate(internalCommandName + "_parameter_" + parameter.getName()) : parameter.getDescription();
                if(parameter.isListParam()) {
                    for (int i = 0; i < parameter.getListSize(); i++) {
                        OptionData optionData = new OptionData(type, parameter.getSlashCompatibleName() + "_" + i, parameterDescription, false);
                        addChoices(optionData, parameter, internalCommandName, isTemplated);
                        optionalParameters.add(optionData);
                    }
                } else {
                    OptionData optionData = new OptionData(type, parameter.getSlashCompatibleName(), parameterDescription, !parameter.isOptional(), parameter.getSupportsAutoComplete());
                    addChoices(optionData, parameter, internalCommandName, isTemplated);
                    requiredParameters.add(optionData);
                }
            }
        });
        requiredParameters.addAll(optionalParameters);
        return requiredParameters;
    }

    private void addChoices(OptionData optionData, Parameter parameter, String commandName, boolean isTemplated) {
        parameter.getChoices().forEach(choiceKey -> {
            String value = isTemplated ? templateService.renderSimpleTemplate(commandName + "_parameter_" + parameter.getName() + "_choice_" + choiceKey) : choiceKey;
            optionData.addChoice(value, choiceKey);
        });
    }

    private boolean shouldParameterBeCreated(Parameter parameter, Long serverId) {
        if(parameter.getDependentFeatures().isEmpty()) {
            return true;
        } else {
            List<FeatureDefinition> featureDefinitions = parameter
                    .getDependentFeatures()
                    .stream()
                    .map(s -> featureConfigService.getFeatureEnum(s))
                    .collect(Collectors.toList());

            for (FeatureDefinition featureDefinition : featureDefinitions) {
                if(!featureFlagService.getFeatureFlagValue(featureDefinition, serverId)) {
                    return false;
                }
            }
            return true;
        }
    }


    @Override
    public CompletableFuture<List<Command>> updateGuildSlashCommand(Guild guild, List<Pair<List<CommandConfiguration>, SlashCommandData>> commandData) {
        List<CommandData> commands = commandData
                .stream()
                .map(Pair::getSecond)
                .collect(Collectors.toList());
        return guild.updateCommands().addCommands(commands).submit().thenApply(createdCommands -> {
          self.storeCreatedSlashCommands(guild, commandData, createdCommands);
          return createdCommands;
        });
    }

    @Override
    public CompletableFuture<Void> deleteGuildSlashCommands(Guild guild, List<Long> slashCommandId, List<Long> commandInServerIds) {
        List<CompletableFuture<Void>> commandFutures = slashCommandId
                .stream()
                .map(commandI -> guild.deleteCommandById(commandI).submit())
                .collect(Collectors.toList());
        return new CompletableFutureList<>(commandFutures).getMainFuture()
                .thenAccept(unused -> self.unsetCommandInServerSlashId(commandInServerIds));
    }

    @Transactional
    public void unsetCommandInServerSlashId(List<Long> commandInServerIds) {
        List<ACommandInAServer> commandsForServer = commandInServerManagementService.getCommandsForServer(commandInServerIds);
        commandsForServer.forEach(aCommandInAServer -> aCommandInAServer.setSlashCommandId(null));
    }

    @Override
    public CompletableFuture<Void> addGuildSlashCommands(Guild guild, List<Pair<List<CommandConfiguration>, SlashCommandData>> commandData) {
        List<CommandData> commands = commandData
                .stream()
                .map(Pair::getSecond)
                .collect(Collectors.toList());
        List<CompletableFuture<Command>> upsertFutures = commands
                .stream()
                .map(upsertCommand -> guild.upsertCommand(upsertCommand).submit())
                .collect(Collectors.toList());
        CompletableFutureList<Command> allFutures = new CompletableFutureList<>(upsertFutures);
        return allFutures.getMainFuture()
                .thenAccept(unused -> self.storeCreatedSlashCommands(guild, commandData, allFutures.getObjects()));
    }

    @Override
    @Transactional
    public void storeCreatedSlashCommands(Guild guild, List<Pair<List<CommandConfiguration>, SlashCommandData>> commandData, List<Command> createdCommands) {
        commandData.forEach(commandConfigurationSlashCommandDataPair -> {
            SlashCommandData slashCommandData = commandConfigurationSlashCommandDataPair.getSecond();
            commandConfigurationSlashCommandDataPair.getFirst().forEach(commandConfiguration -> {
                ACommand aCommand = commandManagementService.findCommandByName(commandConfiguration.getName());
                ACommandInAServer commandInServer = commandInServerManagementService.getCommandForServer(aCommand, guild.getIdLong());
                Command createdCommand = createdCommands.stream().filter(command -> doesCommandMatch(slashCommandData, command)).findFirst().orElse(null);
                if(createdCommand != null) {
                    commandInServer.setSlashCommandId(createdCommand.getIdLong());
                }
            });
        });
    }

    private boolean doesCommandMatch(SlashCommandData commandConfig, Command command) {
        return commandConfig.getName().equals(command.getName());
    }

}

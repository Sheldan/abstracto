package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandReceivedHandler;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import dev.sheldan.abstracto.core.command.exception.CommandNotFoundException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.model.database.ACommandInServerAlias;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class CommandManager implements CommandRegistry {

    public static final String PREFIX = "prefix";
    @Autowired
    private List<Command> commands;

    @Autowired
    private ConfigService configService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Autowired
    private MetricService metricService;

    @Autowired
    private CommandInServerAliasService commandInServerAliasService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public Command findCommandByParameters(String name, UnParsedCommandParameter unParsedCommandParameter, Long serverId) {
        Optional<Command> commandOptional = commands.stream().filter(getCommandByNameAndParameterPredicate(name, unParsedCommandParameter)).findFirst();
        if(!commandOptional.isPresent()) {
            commandOptional = getCommandViaAliasAndParameter(name, unParsedCommandParameter, serverId);
        }
        if(commandOptional.isPresent()){
            return commandOptional.get();
        }
        throw new CommandNotFoundException();
    }

    private Optional<Command> getCommandViaAliasAndParameter(String name, UnParsedCommandParameter unParsedCommandParameter, Long serverId) {
        Optional<ACommandInServerAlias> aliasOptional = commandInServerAliasService.getCommandInServerAlias(serverId, name);
        if(aliasOptional.isPresent()) {
            // if its present, retrieve the original command
            ACommandInAServer command = aliasOptional.get().getCommandInAServer();
            // and find the command based on the newly found name
            return commands.stream().filter(getCommandByNameAndParameterPredicate(command.getCommandReference().getName(), unParsedCommandParameter)).findFirst();
        }
        return Optional.empty();
    }

    private Optional<Command> getCommandViaAlias(String name, Long serverId) {
        Optional<ACommandInServerAlias> aliasOptional = commandInServerAliasService.getCommandInServerAlias(serverId, name);
        if(aliasOptional.isPresent()) {
            // if its present, retrieve the original command
            ACommandInAServer command = aliasOptional.get().getCommandInAServer();
            // and find the command based on the newly found name
            return  commands.stream().filter(getCommandByNamePredicate(command.getCommandReference().getName())).findFirst();
        }
        return Optional.empty();
    }

    private Predicate<Command> getCommandByNameAndParameterPredicate(String name, UnParsedCommandParameter unParsedCommandParameter) {
        return (Command commandObj) -> {
            CommandConfiguration commandConfiguration = commandObj.getConfiguration();
            if (commandConfiguration == null) {
                return false;
            }
            if (!commandNameOrAliasMatches(name, commandConfiguration)) {
                return false;
            }
            return verifyCommandConfiguration(unParsedCommandParameter, commandObj, commandConfiguration);
        };
    }

    private Predicate<Command> getCommandByNamePredicate(String name) {
        return (Command commandObj) -> {
            CommandConfiguration commandConfiguration = commandObj.getConfiguration();
            if (commandConfiguration == null) {
                return false;
            }
            return commandNameOrAliasMatches(name, commandConfiguration);
        };
    }

    private boolean verifyCommandConfiguration(UnParsedCommandParameter unParsedCommandParameter, Command command, CommandConfiguration commandConfiguration) {
        if(commandConfiguration.getParameters() != null && commandConfiguration.getNecessaryParameterCount() > unParsedCommandParameter.getParameters().size()){
            String nextParameterName = commandConfiguration.getParameters().get(commandConfiguration.getNecessaryParameterCount() - 1).getName();
            metricService.incrementCounter(CommandReceivedHandler.COMMANDS_WRONG_PARAMETER_COUNTER);
            throw new InsufficientParametersException(command, nextParameterName);
        }
        return true;
    }

    private boolean commandNameOrAliasMatches(String name, CommandConfiguration commandConfiguration) {
        return commandConfiguration.getName().equalsIgnoreCase(name) ||
                commandConfiguration.getAliases() != null && commandConfiguration.getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(name));
    }

    @Override
    public Command findCommandViaName(String name) {
        return commands.stream().filter((Command o )-> {
            CommandConfiguration commandConfiguration = o.getConfiguration();
            return commandConfiguration.getName().equalsIgnoreCase(name);
        }).findFirst()
        .orElseThrow(CommandNotFoundException::new);
    }

    @Override
    public List<Command> getAllCommands() {
        return commands;
    }

    @Override
    public List<Command> getAllCommandsFromModule(ModuleDefinition moduleDefinition) {
        List<Command> commandsFromModule = new ArrayList<>();
        this.getAllCommands().forEach(command -> {
            CommandConfiguration configuration = command.getConfiguration();
            if(configuration != null && configuration.getModule().equals(moduleDefinition.getInfo().getName())){
                commandsFromModule.add(command);
            }
        });
        return commandsFromModule;
    }

    @Override
    public boolean isCommand(Message message) {
        return message.getContentRaw().startsWith(getPrefix(message.getGuild().getIdLong()));
    }

    @Override
    public boolean commandExists(String name, boolean searchAliases, Long serverId) {
        boolean defaultCommands = commands.stream().anyMatch(findCommandViaCommandConfig(name));
        if(defaultCommands) {
            return true;
        }
        if(searchAliases) {
            return commandInServerAliasService.getCommandInServerAlias(serverId, name).isPresent();
        }
        return false;
    }

    private Predicate<Command> findCommandViaCommandConfig(String name) {
        return command -> command.getConfiguration().getName().equalsIgnoreCase(name) || command.getConfiguration().getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(name));
    }

    @Override
    public Command getCommandByName(String name, boolean searchAliases, Long serverId) {
        Optional<Command> defaultCommand = commands.stream().filter(findCommandViaCommandConfig(name)).findFirst();
        if(defaultCommand.isPresent()) {
            return defaultCommand.get();
        } else {
            if(searchAliases) {
                return getCommandViaAlias(name, serverId).orElse(null);
            }
            return null;
        }
    }

    @Override
    public Optional<Command> getCommandByNameOptional(String name, boolean searchAliases, Long serverId) {
        return Optional.ofNullable(getCommandByName(name, searchAliases, serverId));
    }

    @Override
    public String getCommandName(String input, Long serverId) {
        return input.replaceFirst(getPrefix(serverId), "");
    }

    private String getPrefix(Long serverId) {
        return configService.getStringValue(PREFIX, serverId, getDefaultPrefix());
    }

    private String getDefaultPrefix() {
        return defaultConfigManagementService.getDefaultConfig(PREFIX).getStringValue();
    }
}

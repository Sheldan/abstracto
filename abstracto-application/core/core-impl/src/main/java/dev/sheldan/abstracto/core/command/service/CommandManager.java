package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import dev.sheldan.abstracto.core.command.exception.CommandNotFoundException;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommandManager implements CommandRegistry {

    public static final String PREFIX = "prefix";
    @Autowired
    private List<Command> commands;

    @Autowired
    private ConfigService configService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Override
    public Command findCommandByParameters(String name, UnParsedCommandParameter unParsedCommandParameter) {
        Optional<Command> commandOptional = commands.stream().filter((Command o )-> {
            CommandConfiguration commandConfiguration = o.getConfiguration();
            if(commandConfiguration == null) {
                return false;
            }
            if(!commandNameMatches(name, commandConfiguration)) {
                return false;
            }
            boolean parameterFit;
            if(commandConfiguration.getParameters() != null){
                boolean paramCountFits = unParsedCommandParameter.getParameters().size() >= commandConfiguration.getNecessaryParameterCount();
                boolean hasRemainderParameter = commandConfiguration.getParameters().stream().anyMatch(Parameter::isRemainder);
                if(unParsedCommandParameter.getParameters().size() < commandConfiguration.getNecessaryParameterCount()) {
                    String nextParameterName = commandConfiguration.getParameters().get(commandConfiguration.getNecessaryParameterCount() - 1).getName();
                    throw new InsufficientParametersException(o, nextParameterName);
                }
                parameterFit = paramCountFits || hasRemainderParameter;
            } else {
                parameterFit = unParsedCommandParameter.getParameters().isEmpty();
            }
            return parameterFit;
        }).findFirst();
        if(commandOptional.isPresent()){
            return commandOptional.get();
        }
        throw new CommandNotFoundException();
    }

    private boolean commandNameMatches(String name, CommandConfiguration commandConfiguration) {
        boolean commandNameMatches = commandConfiguration.getName().equalsIgnoreCase(name);
        if(commandNameMatches) {
            return true;
        }
        boolean aliasesMatch = false;
        if(commandConfiguration.getAliases() != null) {
            aliasesMatch = commandConfiguration.getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(name));
        }
        return aliasesMatch;
    }

    public Command findCommand(String name) {
        return commands.stream().filter((Command o )-> {
            CommandConfiguration commandConfiguration = o.getConfiguration();
            return commandConfiguration.getName().equals(name);
        }).findFirst().orElseThrow(CommandNotFoundException::new);
    }

    @Override
    public List<Command> getAllCommands() {
        return commands;
    }

    @Override
    public List<Command> getAllCommandsFromModule(ModuleInterface moduleInterface) {
        List<Command> commandsFromModule = new ArrayList<>();
        this.getAllCommands().forEach(command -> {
            CommandConfiguration configuration = command.getConfiguration();
            if(configuration != null && configuration.getModule().equals(moduleInterface.getInfo().getName())){
                commandsFromModule.add(command);
            }
        });
        return commandsFromModule;
    }

    @Override
    public boolean isCommand(Message message) {
        return message.getContentRaw().startsWith(configService.getStringValue(PREFIX, message.getGuild().getIdLong(), getDefaultPrefix()));
    }

    @Override
    public boolean commandExists(String name) {
        return commands.stream().anyMatch(command -> command.getConfiguration().getName().equalsIgnoreCase(name));
    }

    @Override
    public Command getCommandByName(String name) {
        return commands.stream().filter(command -> command.getConfiguration().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public String getCommandName(String input, Long serverId) {
        return input.replaceFirst(configService.getStringValue(PREFIX, serverId, getDefaultPrefix()), "");
    }

    private String getDefaultPrefix() {
        return defaultConfigManagementService.getDefaultConfig(PREFIX).getStringValue();
    }
}

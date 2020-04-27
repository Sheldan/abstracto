package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import dev.sheldan.abstracto.core.command.exception.CommandNotFound;
import dev.sheldan.abstracto.core.command.exception.InsufficientParameters;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.service.ConfigService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommandManager implements CommandRegistry {

    @Autowired
    private List<Command> commands;

    @Autowired
    private ConfigService configService;

    @Value("${abstracto.prefix}")
    private String defaultPrefix;

    @Override
    public Command findCommandByParameters(String name, UnParsedCommandParameter unParsedCommandParameter) {
        Optional<Command> commandOptional = commands.stream().filter((Command o )-> {
            CommandConfiguration commandConfiguration = o.getConfiguration();
            if(!commandNameMatches(name, commandConfiguration)) {
                return false;
            }
            boolean parameterFit;
            if(commandConfiguration.getParameters() != null){
                boolean paramCountFits = unParsedCommandParameter.getParameters().size() >= commandConfiguration.getNecessaryParameterCount();
                boolean hasRemainderParameter = commandConfiguration.getParameters().stream().anyMatch(Parameter::isRemainder);
                if(unParsedCommandParameter.getParameters().size() < commandConfiguration.getNecessaryParameterCount()) {
                    String nextParameterName = commandConfiguration.getParameters().get(commandConfiguration.getNecessaryParameterCount() - 1).getName();
                    throw new InsufficientParameters("Insufficient parameters", o, nextParameterName);
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
        throw new CommandNotFound("Command not found.");
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
        Optional<Command> commandOptional = commands.stream().filter((Command o )-> {
            CommandConfiguration commandConfiguration = o.getConfiguration();
            return commandConfiguration.getName().equals(name);
        }).findFirst();
        if(commandOptional.isPresent()){
            return commandOptional.get();
        }
        throw new CommandNotFound("Command not found.");
    }

    @Override
    public List<Command> getAllCommands() {
        return commands;
    }

    @Override
    public List<Command> getAllCommandsFromModule(ModuleInterface moduleInterface) {
        List<Command> commandsFromModule = new ArrayList<>();
        this.getAllCommands().forEach(command -> {
            if(command.getConfiguration().getModule().equals(moduleInterface.getInfo().getName())){
                commandsFromModule.add(command);
            }
        });
        return commandsFromModule;
    }

    @Override
    public boolean isCommand(Message message) {
        return message.getContentRaw().startsWith(configService.getStringValue("prefix", message.getGuild().getIdLong(), defaultPrefix));
    }

    @Override
    public String getCommandName(String input, Long serverId) {
        return input.replaceFirst(configService.getStringValue("prefix", serverId, defaultPrefix), "");
    }
}

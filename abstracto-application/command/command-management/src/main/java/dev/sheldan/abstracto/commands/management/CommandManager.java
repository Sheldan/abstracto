package dev.sheldan.abstracto.commands.management;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.Module;
import dev.sheldan.abstracto.command.execution.Configuration;
import dev.sheldan.abstracto.command.execution.Parameter;
import dev.sheldan.abstracto.command.meta.CommandRegistry;
import dev.sheldan.abstracto.command.meta.UnParsedCommandParameter;
import dev.sheldan.abstracto.commands.management.exception.CommandNotFoundException;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommandManager implements CommandRegistry {

    @Autowired
    private List<Command> commands;

    @Override
    public Command findCommandByParameters(String name, UnParsedCommandParameter unParsedCommandParameter) {
        Optional<Command> commandOptional = commands.stream().filter((Command o )-> {
            Configuration configuration = o.getConfiguration();
            boolean parameterFit;
            if(configuration.getParameters() != null){
                boolean paramCountFits = unParsedCommandParameter.getParameters().size() >= configuration.getNecessaryParameterCount();
                boolean hasRemainderParameter = configuration.getParameters().stream().anyMatch(Parameter::isRemainder);
                parameterFit = paramCountFits || hasRemainderParameter;
            } else {
                parameterFit = unParsedCommandParameter.getParameters().size() == 0;
            }
            return configuration.getName().equals(name) && parameterFit;
        }).findFirst();
        if(commandOptional.isPresent()){
            return commandOptional.get();
        }
        throw new CommandNotFoundException();
    }

    public Command findCommand(String name) {
        Optional<Command> commandOptional = commands.stream().filter((Command o )-> {
            Configuration configuration = o.getConfiguration();
            return configuration.getName().equals(name);
        }).findFirst();
        if(commandOptional.isPresent()){
            return commandOptional.get();
        }
        throw new CommandNotFoundException();
    }

    @Override
    public List<Command> getAllCommands() {
        return commands;
    }

    @Override
    public List<Command> getAllCommandsFromModule(Module module) {
        List<Command> commands = new ArrayList<>();
        this.getAllCommands().forEach(command -> {
            if(command.getConfiguration().getModule().equals(module.getInfo().getName())){
                commands.add(command);
            }
        });
        return commands;
    }

    @Override
    public boolean isCommand(Message message) {
        return message.getContentRaw().startsWith("!");
    }
}

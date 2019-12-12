package dev.sheldan.abstracto.command.meta;


import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.Module;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public interface CommandRegistry {
    Command findCommandByParameters(String name, UnParsedCommandParameter context);
    Command findCommand(String message);
    List<Command> getAllCommands();
    List<Command> getAllCommandsFromModule(Module module);
    boolean isCommand(Message message);
}

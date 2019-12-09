package dev.sheldan.abstracto.command.meta;


import dev.sheldan.abstracto.command.Command;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public interface CommandRegistry {
    Command findCommandByParameters(String name, UnParsedCommandParameter context);
    Command findCommand(String message);
    List<Command> getAllCommands();
    boolean isCommand(Message message);
}

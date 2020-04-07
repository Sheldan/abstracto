package dev.sheldan.abstracto.core.command.service;


import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public interface CommandRegistry {
    Command findCommandByParameters(String name, UnParsedCommandParameter context);
    Command findCommand(String message);
    List<Command> getAllCommands();
    List<Command> getAllCommandsFromModule(ModuleInterface module);
    boolean isCommand(Message message);
}

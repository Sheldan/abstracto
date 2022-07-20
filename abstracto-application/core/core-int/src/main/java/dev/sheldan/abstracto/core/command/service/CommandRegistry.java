package dev.sheldan.abstracto.core.command.service;


import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.Optional;

public interface CommandRegistry {
    Optional<Command> findCommandByParameters(String name, UnParsedCommandParameter context, Long serverId);
    Command findCommandViaName(String message);
    List<Command> getAllCommands();
    List<Command> getAllCommandsFromModule(ModuleDefinition module);
    boolean isCommand(Message message);
    boolean commandExists(String name, boolean searchAliases, Long serverId);
    Command getCommandByName(String name, boolean searchAliases, Long serverId);
    Optional<Command> getCommandByNameOptional(String name, boolean searchAliases, Long serverId);
    String getCommandName(String input, Long serverId);
}

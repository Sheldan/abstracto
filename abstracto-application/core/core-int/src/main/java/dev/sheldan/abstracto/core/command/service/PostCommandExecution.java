package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface PostCommandExecution {
    void execute(CommandContext commandContext, CommandResult commandResult, Command command);
    default void executeSlash(SlashCommandInteractionEvent interaction, CommandResult commandResult, Command command) {}
    default boolean supportsSlash() {return false;}
}

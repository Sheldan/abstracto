package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.CompletableFuture;

public interface Command extends FeatureAware {

    default CommandResult execute(CommandContext commandContext) {return CommandResult.fromSuccess();}
    default CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {return CompletableFuture.completedFuture(CommandResult.fromSuccess());}
    default CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) { return CompletableFuture.completedFuture(CommandResult.fromSuccess());}
    CommandConfiguration getConfiguration();
}

package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.concurrent.CompletableFuture;

public interface ContextCommandService {
    CompletableFuture<Command> upsertGuildMessageContextCommand(Guild guild, String name);
    CompletableFuture<Void> deleteGuildContextCommand(Guild guild, Long commandId);
    CompletableFuture<Void> deleteGuildContextCommandByName(Guild guild, String commandName);
}

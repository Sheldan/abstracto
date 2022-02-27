package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class ContextCommandServiceBean implements ContextCommandService {

    @Autowired
    private ApplicationCommandService applicationCommandService;

    @Override
    public CompletableFuture<Command> upsertGuildMessageContextCommand(Guild guild, String name) {
        return guild.upsertCommand(Commands.context(Command.Type.MESSAGE, name)).submit();
    }

    @Override
    public CompletableFuture<Void> deleteGuildContextCommand(Guild guild, Long commandId) {
        return applicationCommandService.deleteGuildCommand(guild, commandId);
    }

    @Override
    public CompletableFuture<Void> deleteGuildContextCommandByName(Guild guild, String commandName) {
        return guild.retrieveCommands().submit().thenCompose(commands -> {
            Optional<Command> foundCommand = commands.stream().filter(command -> command.getType().equals(Command.Type.MESSAGE)).findAny();
            return foundCommand.map(command -> guild.deleteCommandById(command.getIdLong()).submit())
                    .orElseGet(() -> CompletableFuture.completedFuture(null));
        });
    }
}

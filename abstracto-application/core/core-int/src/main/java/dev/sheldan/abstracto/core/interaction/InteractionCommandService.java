package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface InteractionCommandService {
    CompletableFuture<List<Command>> updateGuildCommands(Guild guild, List<Pair<List<CommandConfiguration>, SlashCommandData>> slashCommands, List<ContextCommandConfig> contextCommands);

}

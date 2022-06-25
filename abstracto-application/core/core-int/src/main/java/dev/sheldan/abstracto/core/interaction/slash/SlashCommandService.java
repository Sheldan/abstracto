package dev.sheldan.abstracto.core.interaction.slash;

import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SlashCommandService {
    void convertCommandConfigToCommandData(CommandConfiguration commandConfiguration, List<Pair<List<CommandConfiguration>, SlashCommandData>> existingCommands);
    CompletableFuture<List<Command>> updateGuildSlashCommand(Guild guild, List<Pair<List<CommandConfiguration>, SlashCommandData>> commandData);
    CompletableFuture<Void> deleteGuildSlashCommands(Guild guild, List<Long> slashCommandId, List<Long> commandInServerIdsToUnset);
    CompletableFuture<Void> addGuildSlashCommands(Guild guild, List<Pair<List<CommandConfiguration>, SlashCommandData>> commandData);
    void storeCreatedSlashCommands(Guild guild, List<Pair<List<CommandConfiguration>, SlashCommandData>> commandData, List<Command> createdCommands);
}

package dev.sheldan.abstracto.core.interaction.context;

import dev.sheldan.abstracto.core.interaction.ContextCommandConfig;
import dev.sheldan.abstracto.core.interaction.MessageContextConfig;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.listener.interaction.MessageContextInteractionModel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ContextCommandService {
    CompletableFuture<Void> upsertGuildMessageContextCommand(Guild guild, String name, MessageContextConfig config);
    CompletableFuture<Void> deleteGuildContextCommand(Guild guild, Long commandId);
    CompletableFuture<Void> deleteGuildContextCommandByName(Guild guild, MessageContextConfig contextConfig);
    String getCommandContextName(MessageContextConfig contextConfig, Long guildId);
    boolean matchesGuildContextName(MessageContextInteractionModel model, MessageContextConfig contextConfig, Long guidId);
    void storeCreatedCommands(Command command, AServer server, List<ContextCommandConfig> contextCommands);
}

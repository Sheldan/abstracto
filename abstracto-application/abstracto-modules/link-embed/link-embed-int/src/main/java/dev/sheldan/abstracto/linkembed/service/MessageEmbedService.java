package dev.sheldan.abstracto.linkembed.service;

import dev.sheldan.abstracto.core.models.GuildMemberMessageChannel;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.linkembed.model.MessageEmbedLink;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MessageEmbedService {
    List<MessageEmbedLink> getLinksInMessage(String message);
    void embedLinks(List<MessageEmbedLink> linksToEmbed, GuildMessageChannel target, Long userEmbeddingUserInServerId, GuildMemberMessageChannel executionContext);
    CompletableFuture<Void> embedLink(CachedMessage cachedMessage, GuildMessageChannel target, Long userEmbeddingUserInServerId, GuildMemberMessageChannel executionContext);
    CompletableFuture<Void> embedLink(CachedMessage cachedMessage, GuildMessageChannel target, Long userEmbeddingUserInServerId, GuildMemberMessageChannel executionContext, CommandInteraction interaction);
    CompletableFuture<Void> cleanUpOldMessageEmbeds();
}

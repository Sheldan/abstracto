package dev.sheldan.abstracto.linkembed.service;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.linkembed.model.MessageEmbedLink;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MessageEmbedService {
    List<MessageEmbedLink> getLinksInMessage(String message);
    void embedLinks(List<MessageEmbedLink> linksToEmbed, TextChannel target, Long userEmbeddingUserInServerId, Message embeddingMessage);
    CompletableFuture<Void> embedLink(CachedMessage cachedMessage, TextChannel target, Long userEmbeddingUserInServerId, Message embeddingMessage);
    CompletableFuture<Void> cleanUpOldMessageEmbeds();
}

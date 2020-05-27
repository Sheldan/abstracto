package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;

import java.util.concurrent.CompletableFuture;

public interface MessageCache {
    CompletableFuture<CachedMessage> putMessageInCache(Message message);
    CompletableFuture<CachedMessage> getMessageFromCache(Long guildId, Long textChannelId, Long messageId);
    CompletableFuture<CachedMessage> getMessageFromCache(Message message);
    CompletableFuture<CachedMessage> putMessageInCache(CachedMessage message);
    CompletableFuture<CachedMessage> loadMessage(Long guildId, Long textChannelId, Long messageId);
    CompletableFuture<CachedReaction> getCachedReactionFromReaction(MessageReaction reaction);
    CompletableFuture<CachedMessage> buildCachedMessageFromMessage(Message message);
}

package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.CachedMessage;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface MessageCache {
    CachedMessage putMessageInCache(Message message);
    CachedMessage getMessageFromCache(Message message) throws ExecutionException, InterruptedException;
    CachedMessage getMessageFromCache(Long guildId, Long textChannelId, Long messageId) throws ExecutionException, InterruptedException;
    CompletableFuture<Message> getMessage(Long serverId, Long textChannelId, Long messageId);
}

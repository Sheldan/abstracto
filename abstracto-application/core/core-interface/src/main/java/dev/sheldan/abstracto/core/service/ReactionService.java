package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.CompletableFuture;

public interface ReactionService {
    CompletableFuture<Void> removeReactionFromMessage(CachedReaction reaction, CachedMessage cachedMessage);
    CompletableFuture<Void> removeReactionFromMessage(CachedReaction reaction, Message message);
}

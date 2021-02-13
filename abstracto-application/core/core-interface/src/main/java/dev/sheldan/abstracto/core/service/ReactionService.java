package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import dev.sheldan.abstracto.core.models.database.AEmote;
import net.dv8tion.jda.api.entities.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ReactionService {
    CompletableFuture<Void> removeReactionFromMessage(CachedReaction reaction, CachedMessage cachedMessage);
    CompletableFuture<Void> removeReactionFromMessage(CachedReaction reaction, Message message);
    void addReactionToMessage(String emoteKey, Long serverId, Message message);
    void addDefaultReactionToMessage(String unicode, Message message);
    CompletableFuture<Void> addDefaultReactionToMessageAsync(String unicode, Message message);
    CompletableFuture<Void> addDefaultReactionToMessageAsync(String unicode, Long serverId, Long channelId, Long messageId);
    CompletableFuture<Void> addReactionToMessageAsync(String emoteKey, Long serverId, Message message);
    CompletableFuture<Void> addReactionToMessageAsync(String emoteKey, Guild guild, Message message);
    CompletableFuture<Void> addReactionToMessageAsync(AEmote emote, Long serverId, Message message);
    CompletableFuture<Void> addReactionToMessageAsync(AEmote emote, Guild guild, Message message);
    CompletableFuture<Void> addReactionToMessageAsync(Emote emote,Message message);
    CompletableFuture<Void> addReactionToMessageAsync(Long emoteId, Long serverId, Message message);
    CompletableFuture<Void> addReactionToMessageAsync(String emoteKey, Long serverId, Long channelId, Long messageId);
    CompletableFuture<Void> removeReaction(Message message, String key);
    CompletableFuture<Void> removeReaction(Message message, String key, User user);
    CompletableFuture<Void> removeReaction(Message message, Emote emoteById);
    CompletableFuture<Void> removeReaction(Message message, Emote emoteById, User user);
    CompletableFuture<Void> removeReaction(Message message, CachedEmote cachedEmote, User user);
    CompletableFuture<Void> removeReaction(CachedMessage message, CachedEmote cachedEmote, ServerUser user);
    CompletableFuture<Void> removeReactionFromMessageWithFuture(AEmote emote, Message message);
    CompletableFuture<Void> clearReactionFromMessageWithFuture(AEmote emote, Message message);
    CompletableFuture<Void> clearReaction(Message message, String key);
    CompletableFuture<Void> clearReaction(Message message, Emote emote);
    CompletableFuture<Void> removeReactionFromMessageWithFuture(Integer emoteId, Message message);
    CompletableFuture<Void> clearReactionFromMessageWithFuture(Integer emoteId, Message message);
    CompletableFuture<Void> removeReactionFromMessageWithFuture(AEmote emote, Long serverId, Long channelId, Long messageId);
    CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(AEmote emote, Long serverId, Long channelId, Long messageId, Long userId);
    CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(AEmote emote, Long serverId, Long channelId, Long messageId, Member member);
    CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(AEmote emote, Message message, Member member);
    CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(Integer emoteId, Message message, Member member);
    CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(AEmote emote, Message message, Long userId);
    CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(Integer emoteId, Message message, Long userId);
    CompletableFuture<Void> clearReactionFromMessageWithFuture(AEmote emote, Long serverId, Long channelId, Long messageId);
    List<CompletableFuture<Void>> addReactionsToMessageWithFuture(List<String> emoteKeys, Long serverId, Message message);
}

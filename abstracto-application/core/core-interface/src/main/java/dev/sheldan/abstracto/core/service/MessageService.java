package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import net.dv8tion.jda.api.entities.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MessageService {
    void addReactionToMessage(String emoteKey, Long serverId, Message message);
    void addDefaultReactionToMessage(String unicode, Message message);
    CompletableFuture<Void> addDefaultReactionToMessageAsync(String unicode, Message message);
    CompletableFuture<Void> addDefaultReactionToMessageAsync(String unicode, Long serverId, Long channelId, Long messageId);
    CompletableFuture<Void> addReactionToMessageWithFuture(String emoteKey, Long serverId, Message message);
    CompletableFuture<Void> addReactionToMessageWithFuture(String emoteKey, Guild guild, Message message);
    CompletableFuture<Void> addReactionToMessageWithFuture(AEmote emote, Long serverId, Message message);
    CompletableFuture<Void> addReactionToMessageWithFuture(AEmote emote, Guild guild, Message message);
    CompletableFuture<Void> addReactionToMessageWithFuture(Long emoteId, Long serverId, Message message);
    CompletableFuture<Void> addReactionToMessageWithFuture(String emoteKey, Long serverId, Long channelId, Long messageId);
    CompletableFuture<Void> removeReactionFromMessageWithFuture(AEmote emote, Message message);
    CompletableFuture<Void> clearReactionFromMessageWithFuture(AEmote emote, Message message);
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
    CompletableFuture<Void> deleteMessageInChannelInServer(Long serverId, Long channelId, Long messageId);
    CompletableFuture<Message> createStatusMessage(MessageToSend messageToSend, AChannel channel);
    CompletableFuture<Message> createStatusMessage(MessageToSend messageToSend, MessageChannel channel);
    CompletableFuture<Long> createStatusMessageId(MessageToSend messageToSend, MessageChannel channel);
    void updateStatusMessage(AChannel channel, Long messageId, MessageToSend messageToSend);
    void updateStatusMessage(MessageChannel channel, Long messageId, MessageToSend messageToSend);
    CompletableFuture<Message> sendMessageToUser(AUserInAServer userInAServer, String text);
    CompletableFuture<Message> sendSimpleTemplateToUser(Long userId, String templateKey);
    CompletableFuture<Message> sendTemplateToUser(User user, String template, Object model);
    CompletableFuture<Void> sendEmbedToUser(User user, String template, Object model);
    CompletableFuture<Message> sendEmbedToUserWithMessage(User user, String template, Object model);
    CompletableFuture<Message> sendMessageToSendToUser(User user, MessageToSend messageToSend);
    CompletableFuture<Message> sendMessageToUser(User user, String text);
    CompletableFuture<Void> deleteMessageInChannelWithUser(User user, Long messageId);
    CompletableFuture<Void> editMessageInDMChannel(User user, MessageToSend messageToSend, Long messageId);
    CompletableFuture<Message> loadMessageFromCachedMessage(CachedMessage cachedMessage);
    CompletableFuture<Message> loadMessage(Long serverId, Long channelId, Long messageId);
}

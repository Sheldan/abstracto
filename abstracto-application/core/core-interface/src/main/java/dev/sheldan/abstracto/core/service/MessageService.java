package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MessageService {
    void addReactionToMessage(String emoteKey, Long serverId, Message message);
    CompletableFuture<Void> addReactionToMessageWithFuture(String emoteKey, Long serverId, Message message);
    CompletableFuture<Void> addReactionToMessageWithFuture(AEmote emote, Long serverId, Message message);
    CompletableFuture<Void> addReactionToMessageWithFuture(Long emoteId, Long serverId, Message message);
    CompletableFuture<Void> removeReactionFromMessageWithFuture(AEmote emote, Long serverId, Message message);
    CompletableFuture<Void> clearReactionFromMessageWithFuture(AEmote emote, Message message);
    CompletableFuture<Void> removeReactionFromMessageWithFuture(Integer emoteId, Long serverId, Message message);
    CompletableFuture<Void> clearReactionFromMessageWithFuture(Integer emoteId, Long serverId, Message message);
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
    void sendMessageToUser(AUserInAServer userInAServer, String text, MessageChannel feedbackChannel);
    void sendMessageToUser(User user, String text, MessageChannel feedbackChannel);
}

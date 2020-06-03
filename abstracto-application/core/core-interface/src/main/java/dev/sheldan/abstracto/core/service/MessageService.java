package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MessageService {
    void addReactionToMessage(String emoteKey, Long serverId, Message message);
    CompletableFuture<Void> addReactionToMessageWithFuture(String emoteKey, Long serverId, Message message);
    List<CompletableFuture<Void>> addReactionsToMessageWithFuture(List<String> emoteKeys, Long serverId, Message message);
    CompletableFuture<Void> deleteMessageInChannelInServer(Long serverId, Long channelId, Long messageId);
    CompletableFuture<Message> createStatusMessage(MessageToSend messageToSend, AChannel channel);
    CompletableFuture<Message> createStatusMessage(MessageToSend messageToSend, MessageChannel channel);
    CompletableFuture<Long> createStatusMessageId(MessageToSend messageToSend, MessageChannel channel);
    void updateStatusMessage(AChannel channel, Long messageId, MessageToSend messageToSend);
    void updateStatusMessage(MessageChannel channel, Long messageId, MessageToSend messageToSend);
    void sendMessageToUser(AUserInAServer userInAServer, String text, TextChannel feedbackChannel);
    void sendMessageToUser(User user, String text, TextChannel feedbackChannel);
}

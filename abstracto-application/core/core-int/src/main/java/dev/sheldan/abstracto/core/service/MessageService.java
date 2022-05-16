package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MessageService {
    CompletableFuture<Void> deleteMessageInChannelInServer(Long serverId, Long channelId, Long messageId);
    CompletableFuture<Message> createStatusMessage(MessageToSend messageToSend, AChannel channel);
    CompletableFuture<Message> createStatusMessage(MessageToSend messageToSend, MessageChannel channel);
    CompletableFuture<Long> createStatusMessageId(MessageToSend messageToSend, MessageChannel channel);
    void updateStatusMessage(AChannel channel, Long messageId, MessageToSend messageToSend);
    void updateStatusMessage(MessageChannel channel, Long messageId, MessageToSend messageToSend);
    CompletableFuture<Message> sendMessageToUser(AUserInAServer userInAServer, String text);
    CompletableFuture<Message> sendSimpleTemplateToUser(Long userId, String templateKey);
    List<CompletableFuture<Message>> retrieveMessages(List<ServerChannelMessage> messages);
    CompletableFuture<Message> sendTemplateToUser(User user, String template, Object model);
    CompletableFuture<Void> sendEmbedToUser(User user, String template, Object model);
    CompletableFuture<Message> sendEmbedToUserWithMessage(User user, String template, Object model);
    CompletableFuture<Message> sendMessageToSendToUser(User user, MessageToSend messageToSend);
    CompletableFuture<Message> sendMessageToUser(User user, String text);
    CompletableFuture<Void> deleteMessageInChannelWithUser(User user, Long messageId);
    CompletableFuture<Void> editMessageInDMChannel(User user, MessageToSend messageToSend, Long messageId);
    CompletableFuture<Void> editMessageInChannel(MessageChannel channel, MessageToSend messageToSend, Long messageId);
    CompletableFuture<Message> loadMessageFromCachedMessage(CachedMessage cachedMessage);
    CompletableFuture<Message> loadMessage(Long serverId, Long channelId, Long messageId);
    CompletableFuture<Message> loadMessage(Message message);
    CompletableFuture<Message> editMessageWithNewTemplate(Message message, String templateKey, Object model);
    MessageAction editMessage(Message message, MessageEmbed messageEmbed);
    MessageAction editMessage(Message message, String text, MessageEmbed messageEmbed);
    AuditableRestAction<Void> deleteMessageWithAction(Message message);
    CompletableFuture<Void> deleteMessage(Message message);
    CompletableFuture<Void> editMessageWithActionRows(Message message, List<ActionRow> rows);
    CompletableFuture<Message> editMessageWithActionRowsMessage(Message message, List<ActionRow> rows);
    CompletableFuture<Void> pinMessage(Message message);
}

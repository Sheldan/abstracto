package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ChannelService {
    void sendTextToAChannelNotAsync(String text, AChannel channel);
    void sendTextToChannelNotAsync(String text, MessageChannel channel);
    CompletableFuture<Message> sendTextToAChannel(String text, AChannel channel);
    CompletableFuture<Message> sendMessageToAChannel(Message message, AChannel channel);
    CompletableFuture<Message> sendMessageToChannel(Message message, MessageChannel channel);
    CompletableFuture<Message> sendTextToChannel(String text, MessageChannel channel);
    CompletableFuture<Message> sendEmbedToAChannel(MessageEmbed embed, AChannel channel);
    CompletableFuture<Message> sendEmbedToChannel(MessageEmbed embed, MessageChannel channel);
    MessageAction sendEmbedToChannelInComplete(MessageEmbed embed, MessageChannel channel);
    List<CompletableFuture<Message>> sendMessageEmbedToSendToAChannel(MessageToSend messageToSend, AChannel channel);
    CompletableFuture<Message> sendMessageEmbedToSendToAChannel(MessageToSend messageToSend, AChannel channel, Integer embedIndex);

    CompletableFuture<Message> retrieveMessageInChannel(Long serverId, Long channelId, Long messageId);
    CompletableFuture<Message> retrieveMessageInChannel(MessageChannel channel, Long messageId);

    /**
     * Sends a {@link MessageToSend} to Discord. This can result in multiple messages, because we do support multiple embeds.
     * @param messageToSend The {@link MessageToSend} to send, this can contain multiple embeds and normal text. The text will be sent
     *                      in the first message. One optional file can be used as an attachment. This will also be present in the first message.
     * @param textChannel The {@link MessageChannel} to send the messages to
     * @return A list of {@link CompletableFuture} representing each potential message sent
     */
    List<CompletableFuture<Message>> sendMessageToSendToChannel(MessageToSend messageToSend, MessageChannel textChannel);
    void editMessageInAChannel(MessageToSend messageToSend, AChannel channel, Long messageId);
    void editMessageInAChannel(MessageToSend messageToSend, MessageChannel channel, Long messageId);
    CompletableFuture<Message> editMessageInAChannelFuture(MessageToSend messageToSend, MessageChannel channel, Long messageId);
    CompletableFuture<Message> editEmbedMessageInAChannel(MessageEmbed embedToSend, MessageChannel channel, Long messageId);
    CompletableFuture<Message> editTextMessageInAChannel(String text, MessageChannel channel, Long messageId);
    CompletableFuture<Message> editTextMessageInAChannel(String text, MessageEmbed messageEmbed, MessageChannel channel, Long messageId);
    List<CompletableFuture<Message>> editMessagesInAChannelFuture(MessageToSend messageToSend, MessageChannel channel, List<Long> messageIds);
    CompletableFuture<Message> removeFieldFromMessage(MessageChannel channel, Long messageId, Integer index);
    CompletableFuture<Message> removeFieldFromMessage(MessageChannel channel, Long messageId, Integer index, Integer embedIndex);
    CompletableFuture<Void> deleteTextChannel(AChannel channel);
    CompletableFuture<Void> deleteTextChannel(Long serverId, Long channelId);
    List<CompletableFuture<Message>> sendEmbedTemplateInTextChannelList(String templateKey, Object model, TextChannel channel);
    List<CompletableFuture<Message>> sendEmbedTemplateInMessageChannelList(String templateKey, Object model, MessageChannel channel);
    CompletableFuture<Message> sendTextTemplateInTextChannel(String templateKey, Object model, TextChannel channel);
    CompletableFuture<Message> sendTextTemplateInMessageChannel(String templateKey, Object model, MessageChannel channel);
    RestAction<Void> deleteMessagesInChannel(TextChannel textChannel, List<Message> messages);

    CompletableFuture<TextChannel> createTextChannel(String name, AServer server, Long categoryId);
    Optional<TextChannel> getChannelFromAChannel(AChannel channel);
    AChannel getFakeChannelFromTextChannel(TextChannel textChannel);
    CompletableFuture<Message> sendSimpleTemplateToChannel(Long serverId, Long channelId, String template);
    CompletableFuture<MessageHistory> getHistoryOfChannel(TextChannel channel, Long startMessageId, Integer amount);
    Optional<TextChannel> getTextChannelFromServerOptional(Guild serverId, Long textChannelId);
    TextChannel getTextChannelFromServer(Guild guild, Long textChannelId);
    TextChannel getTextChannelFromServerNullable(Guild guild, Long textChannelId);
    Optional<TextChannel> getTextChannelFromServerOptional(Long serverId, Long textChannelId);
    TextChannel getTextChannelFromServer(Long serverId, Long textChannelId);
    CompletableFuture<Void> setSlowModeInChannel(TextChannel textChannel, Integer seconds);
    List<CompletableFuture<Message>> sendFileToChannel(String fileContent, String fileNameTemplate, String messageTemplate, Object model, TextChannel channel);
}

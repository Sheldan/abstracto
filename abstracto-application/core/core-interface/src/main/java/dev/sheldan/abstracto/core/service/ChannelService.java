package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.models.database.AChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ChannelService {
    void sendTextToAChannelNoFuture(String text, AChannel channel);
    void sendTextToChannelNoFuture(String text, MessageChannel channel);
    CompletableFuture<Message> sendTextToAChannel(String text, AChannel channel);
    CompletableFuture<Message> sendMessageToAChannel(Message message, AChannel channel);
    CompletableFuture<Message> sendMessageToChannel(Message message, MessageChannel channel);
    CompletableFuture<Message> sendTextToChannel(String text, MessageChannel channel);
    CompletableFuture<Message> sendEmbedToAChannel(MessageEmbed embed, AChannel channel);
    CompletableFuture<Message> sendEmbedToChannel(MessageEmbed embed, MessageChannel channel);
    List<CompletableFuture<Message>> sendMessageToSendToAChannel(MessageToSend messageToSend, AChannel channel);
    List<CompletableFuture<Message>> sendMessageToSendToChannel(MessageToSend messageToSend, MessageChannel textChannel);
    Optional<TextChannel> getTextChannelInGuild(Long serverId, Long channelId);
    void editMessageInAChannel(MessageToSend messageToSend, AChannel channel, Long messageId);
    void editMessageInAChannel(MessageToSend messageToSend, MessageChannel channel, Long messageId);
    CompletableFuture<Void> deleteTextChannel(AChannel channel);
    CompletableFuture<Void> deleteTextChannel(Long serverId, Long channelId);
    List<CompletableFuture<Message>> sendTemplateInChannel(String templateKey, Object model, MessageChannel channel);

    CompletableFuture<TextChannel> createTextChannel(String name, AServer server, Long categoryId);
}

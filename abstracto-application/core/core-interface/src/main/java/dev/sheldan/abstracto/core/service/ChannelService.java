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
    void sendTextInAChannel(String text, AChannel channel);
    void sendTextInAChannel(String text, MessageChannel channel);
    CompletableFuture<Message> sendTextInAChannelFuture(String text, AChannel channel);
    CompletableFuture<Message> sendTextInAChannelFuture(String text, MessageChannel channel);
    CompletableFuture<Message> sendEmbedInAChannelFuture(MessageEmbed embed, AChannel channel);
    CompletableFuture<Message> sendEmbedInAChannelFuture(MessageEmbed embed, MessageChannel channel);
    List<CompletableFuture<Message>> sendMessageToEndInAChannel(MessageToSend messageToSend, AChannel channel);
    List<CompletableFuture<Message>> sendMessageToEndInTextChannel(MessageToSend messageToSend, MessageChannel textChannel);
    Optional<TextChannel> getTextChannelInGuild(Long serverId, Long channelId);
    void editMessageInAChannel(MessageToSend messageToSend, AChannel channel, Long messageId);
    void editMessageInAChannel(MessageToSend messageToSend, MessageChannel channel, Long messageId);

    CompletableFuture<TextChannel> createTextChannel(String name, AServer server, Long categoryId);
}

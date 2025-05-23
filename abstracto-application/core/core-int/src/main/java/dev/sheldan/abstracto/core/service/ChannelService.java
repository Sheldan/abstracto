package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ChannelService {
    void sendTextToAChannelNotAsync(String text, AChannel channel);
    void sendTextToChannelNotAsync(String text, MessageChannel channel);
    CompletableFuture<ThreadChannel> createThreadChannel(IThreadContainer threadContainer, String name);
    CompletableFuture<ThreadChannel> createThreadChannel(IThreadContainer threadContainer, String name, Long messageId);
    CompletableFuture<Message> sendTextToAChannel(String text, AChannel channel);
    CompletableFuture<Message> sendMessageToAChannel(Message message, AChannel channel);
    CompletableFuture<Message> sendMessageToChannel(Message message, GuildMessageChannel channel);
    CompletableFuture<Message> sendTextToChannel(String text, MessageChannel channel);
    CompletableFuture<Message> sendEmbedToAChannel(MessageEmbed embed, AChannel channel);
    CompletableFuture<Message> sendEmbedToChannel(MessageEmbed embed, MessageChannel channel);
    MessageCreateAction sendEmbedToChannelInComplete(MessageEmbed embed, MessageChannel channel);
    List<CompletableFuture<Message>> sendMessageEmbedToSendToAChannel(MessageToSend messageToSend, AChannel channel);
    CompletableFuture<Message> sendMessageEmbedToSendToAChannel(MessageToSend messageToSend, AChannel channel, Integer embedIndex);

    CompletableFuture<Message> retrieveMessageInChannel(Long serverId, Long channelId, Long messageId);
    CompletableFuture<Message> retrieveMessageInChannel(MessageChannel channel, Long messageId);

    List<CompletableFuture<Message>> sendMessageToSendToChannel(MessageToSend messageToSend, MessageChannel messageChannel);
    void editMessageInAChannel(MessageToSend messageToSend, AChannel channel, Long messageId);
    void editMessageInAChannel(MessageToSend messageToSend, MessageChannel channel, Long messageId);
    CompletableFuture<Message> editMessageInAChannelFuture(MessageToSend messageToSend, MessageChannel channel, Long messageId);
    CompletableFuture<Message> editMessageInAChannelFuture(MessageToSend messageToSend, Long serverId, Long channelId, Long messageId);
    CompletableFuture<Message> editEmbedMessageInAChannel(MessageEmbed embedToSend, MessageChannel channel, Long messageId);
    CompletableFuture<Message> editTextMessageInAChannel(String text, MessageChannel channel, Long messageId);
    CompletableFuture<Message> editTextMessageInAChannel(String text, MessageEmbed messageEmbed, MessageChannel channel, Long messageId);
    List<CompletableFuture<Message>> editMessagesInAChannelFuture(MessageToSend messageToSend, MessageChannel channel, List<Long> messageIds);
    CompletableFuture<Message> removeFieldFromMessage(MessageChannel channel, Long messageId, Integer index);
    CompletableFuture<Message> editFieldValueInMessage(MessageChannel channel, Long messageId, Integer index, String newValue);
    CompletableFuture<Message> removeFieldFromMessage(MessageChannel channel, Long messageId, Integer index, Integer embedIndex);
    CompletableFuture<Message> removeComponents(MessageChannel channel, Long messageId);
    ThreadChannel getThreadChannel(Long threadChannelId);
    CompletableFuture<Void> archiveThreadChannel(ThreadChannel threadChannel);
    CompletableFuture<Void> deleteTextChannel(AChannel channel);
    CompletableFuture<Void> deleteTextChannel(Long serverId, Long channelId);
    List<CompletableFuture<Message>> sendEmbedTemplateInMessageChannel(String templateKey, Object model, MessageChannel channel);
    CompletableFuture<Message> sendTextTemplateInMessageChannel(String templateKey, Object model, MessageChannel channel);
    CompletableFuture<Void> deleteMessagesInChannel(MessageChannel messageChannel, List<Message> messages);

    CompletableFuture<TextChannel> createTextChannel(String name, AServer server, Long categoryId);
    CompletableFuture<ThreadChannel> createThread(TextChannel textChannel, String name);
    CompletableFuture<ThreadChannel> createThreadWithStarterMessage(TextChannel textChannel, String name, Long messageId);
    CompletableFuture<ThreadChannel> createPrivateThread(TextChannel textChannel, String name);
    Optional<GuildChannel> getChannelFromAChannel(AChannel channel);
    Optional<GuildMessageChannel> getGuildMessageChannelFromAChannelOptional(AChannel channel);
    GuildMessageChannel getGuildMessageChannelFromAChannel(AChannel channel);
    AChannel getFakeChannelFromTextChannel(MessageChannel messageChannel);
    CompletableFuture<Message> sendSimpleTemplateToChannel(Long serverId, Long channelId, String template);
    CompletableFuture<MessageHistory> getHistoryOfChannel(MessageChannel channel, Long startMessageId, Integer amount);
    Optional<GuildChannel> getGuildChannelFromServerOptional(Guild serverId, Long textChannelId);
    GuildMessageChannel getMessageChannelFromServer(Guild guild, Long textChannelId);

    GuildMessageChannel getMessageChannelFromServer(Long serverId, Long textChannelId);
    Optional<GuildMessageChannel> getMessageChannelFromServerOptional(Long serverId, Long textChannelId);

    GuildMessageChannel getMessageChannelFromServerNullable(Guild guild, Long textChannelId);
    Optional<GuildChannel> getGuildChannelFromServerOptional(Long serverId, Long textChannelId);

    GuildChannel getGuildChannelFromServer(Long serverId, Long channelId);

    Optional<TextChannel> getTextChannelFromServerOptional(Long serverId, Long textChannelId);
    CompletableFuture<Void> setSlowModeInChannel(TextChannel textChannel, Integer seconds);
    List<CompletableFuture<Message>> sendFileToChannel(String fileContent, String fileNameTemplate, String messageTemplate, Object model, MessageChannel channel);
    List<CompletableFuture<Message>> sendFileToChannel(String fileContent, String fileName, MessageChannel channel);

    CompletableFuture<Void> addMemberViewToChannel(Guild guild, Long channelId, Long memberId, Collection<Permission> permissions);
    CompletableFuture<Void> removeChannelOverrideForMember(Guild guild, Long channelId, Long memberId);
}

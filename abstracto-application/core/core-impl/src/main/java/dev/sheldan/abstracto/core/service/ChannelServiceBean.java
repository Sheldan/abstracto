package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.CategoryNotFoundException;
import dev.sheldan.abstracto.core.exception.ChannelNotInGuildException;
import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class ChannelServiceBean implements ChannelService {

    @Autowired
    private BotService botService;

    @Autowired
    private TemplateService templateService;

    @Override
    public void sendTextToAChannelNotAsync(String text, AChannel channel) {
        sendTextToAChannel(text, channel);
    }

    @Override
    public void sendTextToChannelNotAsync(String text, MessageChannel channel) {
        sendTextToChannel(text, channel);
    }

    @Override
    public CompletableFuture<Message> sendTextToAChannel(String text, AChannel channel) {
        Guild guild = botService.getInstance().getGuildById(channel.getServer().getId());
        if (guild != null) {
            TextChannel textChannel = guild.getTextChannelById(channel.getId());
            if(textChannel != null) {
                return sendTextToChannel(text, textChannel);
            } else {
                log.error("Channel {} to post towards was not found in server {}", channel.getId(), channel.getServer().getId());
                throw new ChannelNotInGuildException(channel.getId());
            }
        } else {
            log.error("Guild {} was not found when trying to post a message", channel.getServer().getId());
            throw new GuildNotFoundException(channel.getServer().getId());
        }
    }

    @Override
    public CompletableFuture<Message> sendMessageToAChannel(Message message, AChannel channel) {
        Optional<TextChannel> textChannelOpt = botService.getTextChannelFromServerOptional(channel.getServer().getId(), channel.getId());
        if(textChannelOpt.isPresent()) {
            TextChannel textChannel = textChannelOpt.get();
            return sendMessageToChannel(message, textChannel);
        }
        throw new ChannelNotInGuildException(channel.getId());
    }

    @Override
    public CompletableFuture<Message> sendMessageToChannel(Message message, MessageChannel channel) {
        log.trace("Sending message {} from channel {} and server {} to channel {}.",
                message.getId(), message.getChannel().getId(), message.getGuild().getId(), channel.getId());
        return channel.sendMessage(message).submit();
    }

    @Override
    public CompletableFuture<Message> sendTextToChannel(String text, MessageChannel channel) {
        log.trace("Sending text to channel {}.", channel.getId());
        return channel.sendMessage(text).submit();
    }

    @Override
    public CompletableFuture<Message> sendEmbedToAChannel(MessageEmbed embed, AChannel channel) {
        Guild guild = botService.getInstance().getGuildById(channel.getServer().getId());
        if (guild != null) {
            TextChannel textChannel = guild.getTextChannelById(channel.getId());
            if(textChannel != null) {
                return sendEmbedToChannel(embed, textChannel);
            } else {
                log.error("Channel {} to post towards was not found in server {}", channel.getId(), channel.getServer().getId());
                throw new ChannelNotInGuildException(channel.getId());
            }
        } else {
            log.error("Guild {} was not found when trying to post a message", channel.getServer().getId());
            throw new GuildNotFoundException(channel.getServer().getId());
        }
    }

    @Override
    public CompletableFuture<Message> sendEmbedToChannel(MessageEmbed embed, MessageChannel channel) {
        log.trace("Sending embed to channel {}.", channel.getId());
        return sendEmbedToChannelInComplete(embed, channel).submit();
    }

    @Override
    public MessageAction sendEmbedToChannelInComplete(MessageEmbed embed, MessageChannel channel) {
        return channel.sendMessage(embed);
    }

    @Override
    public List<CompletableFuture<Message>> sendMessageToSendToAChannel(MessageToSend messageToSend, AChannel channel) {
        Optional<TextChannel> textChannelFromServer = botService.getTextChannelFromServerOptional(channel.getServer().getId(), channel.getId());
        if(textChannelFromServer.isPresent()) {
            return sendMessageToSendToChannel(messageToSend, textChannelFromServer.get());
        }
        throw new ChannelNotInGuildException(channel.getId());
    }

    @Override
    public CompletableFuture<Message> sendMessageToSendToAChannel(MessageToSend messageToSend, AChannel channel, Integer embedIndex) {
        return sendEmbedToAChannel(messageToSend.getEmbeds().get(embedIndex), channel);
    }

    @Override
    public List<CompletableFuture<Message>> sendMessageToSendToChannel(MessageToSend messageToSend, MessageChannel textChannel) {
        String messageText = messageToSend.getMessage();
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        MessageAction firstMessageAction = null;
        List<MessageAction> allMessageActions = new ArrayList<>();
        if(!StringUtils.isBlank(messageText)) {
            firstMessageAction = textChannel.sendMessage(messageText);
        }
        if(!messageToSend.getEmbeds().isEmpty()) {
            if(firstMessageAction != null) {
                firstMessageAction.embed(messageToSend.getEmbeds().get(0));
            } else {
                firstMessageAction = textChannel.sendMessage(messageToSend.getEmbeds().get(0));
            }
            messageToSend.getEmbeds().stream().skip(1).forEach(embed -> allMessageActions.add(sendEmbedToChannelInComplete(embed, textChannel)));
        }
        if(messageToSend.hasFileToSend()) {
            if(firstMessageAction != null) {
                firstMessageAction.addFile(messageToSend.getFileToSend());
            } else {
                firstMessageAction = textChannel.sendFile(messageToSend.getFileToSend());
            }
        }
        allMessageActions.add(0, firstMessageAction);
        allMessageActions.forEach(messageAction ->
            futures.add(messageAction.submit())
        );
        return futures;
    }

    @Override
    public Optional<TextChannel> getTextChannelInGuild(Long serverId, Long channelId) {
        return botService.getTextChannelFromServerOptional(serverId, channelId);
    }

    @Override
    public void editMessageInAChannel(MessageToSend messageToSend, AChannel channel, Long messageId) {
        Optional<TextChannel> textChannelFromServer = botService.getTextChannelFromServerOptional(channel.getServer().getId(), channel.getId());
        if(textChannelFromServer.isPresent()) {
            TextChannel textChannel = textChannelFromServer.get();
            editMessageInAChannel(messageToSend, textChannel, messageId);
        } else {
            throw new ChannelNotInGuildException(channel.getId());
        }
    }

    @Override
    public void editMessageInAChannel(MessageToSend messageToSend, MessageChannel channel, Long messageId) {
       editMessageInAChannelFuture(messageToSend, channel, messageId);
    }

    @Override
    public CompletableFuture<Message> editMessageInAChannelFuture(MessageToSend messageToSend, MessageChannel channel, Long messageId) {
        MessageAction messageAction;
        if(!StringUtils.isBlank(messageToSend.getMessage())) {
            log.trace("Editing message {} with new text content.", messageId);
            messageAction = channel.editMessageById(messageId, messageToSend.getMessage());
            if(messageToSend.getEmbeds() != null && !messageToSend.getEmbeds().isEmpty()) {
                log.trace("Also editing the embed for message {}.", messageId);
                messageAction = messageAction.embed(messageToSend.getEmbeds().get(0));
            }
        } else {
            log.trace("Editing message {} with new embeds.", messageId);
            if(messageToSend.getEmbeds() != null && !messageToSend.getEmbeds().isEmpty()) {
                messageAction = channel.editMessageById(messageId, messageToSend.getEmbeds().get(0));
            } else {
                throw new IllegalArgumentException("Message to send did not contain anything to send.");
            }
        }
        return messageAction.submit();
    }

    @Override
    public CompletableFuture<Message> editEmbedMessageInAChannel(MessageEmbed embedToSend, MessageChannel channel, Long messageId) {
        return channel.editMessageById(messageId, embedToSend).submit();
    }

    @Override
    public CompletableFuture<Message> editTextMessageInAChannel(String text, MessageChannel channel, Long messageId) {
        return channel.editMessageById(messageId, text).submit();
    }

    @Override
    public List<CompletableFuture<Message>> editMessagesInAChannelFuture(MessageToSend messageToSend, MessageChannel channel, List<Long> messageIds) {
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        futures.add(editMessageInAChannelFuture(messageToSend, channel ,messageIds.get(0)));
        for (int i = 1; i < messageIds.size(); i++) {
            Long messageIdToUpdate = messageIds.get(i);
            futures.add(channel.editMessageById(messageIdToUpdate, messageToSend.getEmbeds().get(i)).submit());
        }
        return futures;
    }

    @Override
    public CompletableFuture<Message> removeFieldFromMessage(MessageChannel channel, Long messageId, Integer index) {
        return removeFieldFromMessage(channel, messageId, index, 0);
    }

    @Override
    public CompletableFuture<Message> removeFieldFromMessage(MessageChannel channel, Long messageId, Integer index, Integer embedIndex) {
        return channel.retrieveMessageById(messageId).submit().thenCompose(message -> {
            EmbedBuilder embedBuilder = new EmbedBuilder(message.getEmbeds().get(embedIndex));
            embedBuilder.getFields().remove(index.intValue());
            log.trace("Removing field with index {} from message {}.", index, messageId);
            return channel.editMessageById(messageId, embedBuilder.build()).submit();
        });
    }

    @Override
    public CompletableFuture<Void> deleteTextChannel(AChannel channel) {
       return deleteTextChannel(channel.getServer().getId(), channel.getId());
    }

    @Override
    public CompletableFuture<Void> deleteTextChannel(Long serverId, Long channelId) {
        TextChannel textChannelById = botService.getInstance().getTextChannelById(channelId);
        if(textChannelById != null) {
            log.info("Deleting channel {} on server {}.", channelId, serverId);
            return textChannelById.delete().submit();
        }
        throw new ChannelNotInGuildException(channelId);
    }

    @Override
    @Transactional
    public List<CompletableFuture<Message>> sendEmbedTemplateInChannel(String templateKey, Object model, MessageChannel channel) {
        MessageToSend messageToSend = templateService.renderEmbedTemplate(templateKey, model);
        return sendMessageToSendToChannel(messageToSend, channel);
    }

    @Override
    public CompletableFuture<Message> sendTextTemplateInChannel(String templateKey, Object model, MessageChannel channel) {
        String text = templateService.renderTemplate(templateKey, model);
        return sendTextToChannel(text, channel);
    }

    @Override
    public CompletableFuture<TextChannel> createTextChannel(String name, AServer server, Long categoryId) {
        Optional<Guild> guildById = botService.getGuildByIdOptional(server.getId());
        if(guildById.isPresent()) {
            Guild guild = guildById.get();
            Category categoryById = guild.getCategoryById(categoryId);
            if(categoryById != null) {
                log.info("Creating channel on server {} in category {}.", server.getId(), categoryById);
                return categoryById.createTextChannel(name).submit();
            }
            throw new CategoryNotFoundException(categoryId, server.getId());
        }
        throw new GuildNotFoundException(server.getId());
    }

    @Override
    public Optional<TextChannel> getChannelFromAChannel(AChannel channel) {
        return botService.getTextChannelFromServerOptional(channel.getServer().getId(), channel.getId());
    }

    @Override
    public AChannel getFakeChannelFromTextChannel(TextChannel textChannel) {
        AServer server = AServer
                .builder()
                .id(textChannel.getIdLong())
                .fake(true)
                .build();
        return AChannel
                .builder()
                .fake(true)
                .id(textChannel.getIdLong())
                .server(server)
                .build();
    }
}

package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.*;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FileService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.core.config.MetricConstants.DISCORD_API_INTERACTION_METRIC;
import static dev.sheldan.abstracto.core.config.MetricConstants.INTERACTION_TYPE;
import static dev.sheldan.abstracto.core.service.MessageServiceBean.*;

@Slf4j
@Component
public class ChannelServiceBean implements ChannelService {

    @Autowired
    private GuildService guildService;

    @Autowired
    private BotService botService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private AllowedMentionService allowedMentionService;

    @Autowired
    private FileService fileService;

    @Autowired
    private MetricService metricService;

    public static final CounterMetric CHANNEL_CREATE_METRIC = CounterMetric
            .builder()
            .name(DISCORD_API_INTERACTION_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(INTERACTION_TYPE, "channel.create")))
            .build();

    public static final CounterMetric CHANNEL_DELETE_METRIC = CounterMetric
            .builder()
            .name(DISCORD_API_INTERACTION_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(INTERACTION_TYPE, "channel.delete")))
            .build();

    public static final CounterMetric CHANNEL_MESSAGE_BULK_DELETE_METRIC = CounterMetric
            .builder()
            .name(DISCORD_API_INTERACTION_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(INTERACTION_TYPE, "channel.message.bulk.delete")))
            .build();

    public static final CounterMetric CHANNEL_RETRIEVE_HISTORY = CounterMetric
            .builder()
            .name(DISCORD_API_INTERACTION_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(INTERACTION_TYPE, "channel.load.history")))
            .build();

    public static final CounterMetric CHANNEL_CHANGE_SLOW_MODE = CounterMetric
            .builder()
            .name(DISCORD_API_INTERACTION_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(INTERACTION_TYPE, "channel.slowmode")))
            .build();

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
        Optional<TextChannel> textChannelOpt = getTextChannelFromServerOptional(channel.getServer().getId(), channel.getId());
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
        metricService.incrementCounter(MESSAGE_SEND_METRIC);
        return channel.sendMessage(message).allowedMentions(getAllowedMentionsFor(channel)).submit();
    }

    private List<Message.MentionType> getAllowedMentionsFor(MessageChannel channel) {
        if(channel instanceof GuildChannel) {
            return allowedMentionService.getAllowedMentionTypesForServer(((GuildChannel) channel).getGuild().getIdLong());
        }
        return null;
    }

    @Override
    public CompletableFuture<Message> sendTextToChannel(String text, MessageChannel channel) {
        log.trace("Sending text to channel {}.", channel.getId());
        metricService.incrementCounter(MESSAGE_SEND_METRIC);
        return channel.sendMessage(text).allowedMentions(getAllowedMentionsFor(channel)).submit();
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
        metricService.incrementCounter(MESSAGE_SEND_METRIC);
        return channel.sendMessage(embed).allowedMentions(getAllowedMentionsFor(channel));
    }

    @Override
    public List<CompletableFuture<Message>> sendMessageToSendToAChannel(MessageToSend messageToSend, AChannel channel) {
        Optional<TextChannel> textChannelFromServer = getTextChannelFromServerOptional(channel.getServer().getId(), channel.getId());
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
    public CompletableFuture<Message> retrieveMessageInChannel(Long serverId, Long channelId, Long messageId) {
        TextChannel channel = getTextChannelFromServer(serverId, channelId);
        return retrieveMessageInChannel(channel, messageId);
    }

    @Override
    public CompletableFuture<Message> retrieveMessageInChannel(MessageChannel channel, Long messageId) {
        metricService.incrementCounter(MESSAGE_LOAD_METRIC);
        return channel.retrieveMessageById(messageId).submit();
    }

    @Override
    public List<CompletableFuture<Message>> sendMessageToSendToChannel(MessageToSend messageToSend, MessageChannel textChannel) {
        String messageText = messageToSend.getMessage();
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        MessageAction firstMessageAction = null;
        List<MessageAction> allMessageActions = new ArrayList<>();
        if(!StringUtils.isBlank(messageText)) {
            metricService.incrementCounter(MESSAGE_SEND_METRIC);
            firstMessageAction = textChannel.sendMessage(messageText);
        }
        if(!messageToSend.getEmbeds().isEmpty()) {
            if(firstMessageAction != null) {
                metricService.incrementCounter(MESSAGE_SEND_METRIC);
                firstMessageAction.embed(messageToSend.getEmbeds().get(0));
            } else {
                firstMessageAction = textChannel.sendMessage(messageToSend.getEmbeds().get(0));
            }
            messageToSend.getEmbeds().stream().skip(1).forEach(embed -> allMessageActions.add(sendEmbedToChannelInComplete(embed, textChannel)));
        }
        if(messageToSend.hasFileToSend()) {
            if(firstMessageAction != null) {
                // in case there has not been a message, we need to increment it
                metricService.incrementCounter(MESSAGE_SEND_METRIC);
                firstMessageAction.addFile(messageToSend.getFileToSend());
            } else {
                firstMessageAction = textChannel.sendFile(messageToSend.getFileToSend());
            }
        }
        allMessageActions.add(0, firstMessageAction);
        List<Message.MentionType> allowedMentions = getAllowedMentionsFor(textChannel);
        allMessageActions.forEach(messageAction ->
            futures.add(messageAction.allowedMentions(allowedMentions).submit())
        );
        return futures;
    }

    @Override
    public void editMessageInAChannel(MessageToSend messageToSend, AChannel channel, Long messageId) {
        Optional<TextChannel> textChannelFromServer = getTextChannelFromServerOptional(channel.getServer().getId(), channel.getId());
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
        metricService.incrementCounter(MESSAGE_EDIT_METRIC);
        return messageAction.submit();
    }

    @Override
    public CompletableFuture<Message> editEmbedMessageInAChannel(MessageEmbed embedToSend, MessageChannel channel, Long messageId) {
        metricService.incrementCounter(MESSAGE_EDIT_METRIC);
        return channel.editMessageById(messageId, embedToSend).submit();
    }

    @Override
    public CompletableFuture<Message> editTextMessageInAChannel(String text, MessageChannel channel, Long messageId) {
        metricService.incrementCounter(MESSAGE_EDIT_METRIC);
        return channel.editMessageById(messageId, text).submit();
    }

    @Override
    public CompletableFuture<Message> editTextMessageInAChannel(String text, MessageEmbed messageEmbed, MessageChannel channel, Long messageId) {
        metricService.incrementCounter(MESSAGE_EDIT_METRIC);
        return channel.editMessageById(messageId, text).embed(messageEmbed).submit();
    }

    @Override
    public List<CompletableFuture<Message>> editMessagesInAChannelFuture(MessageToSend messageToSend, MessageChannel channel, List<Long> messageIds) {
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        futures.add(editMessageInAChannelFuture(messageToSend, channel ,messageIds.get(0)));
        for (int i = 1; i < messageIds.size(); i++) {
            Long messageIdToUpdate = messageIds.get(i);
            futures.add(editEmbedMessageInAChannel(messageToSend.getEmbeds().get(i), channel, messageIdToUpdate));
        }
        return futures;
    }

    @Override
    public CompletableFuture<Message> removeFieldFromMessage(MessageChannel channel, Long messageId, Integer index) {
        return removeFieldFromMessage(channel, messageId, index, 0);
    }

    @Override
    public CompletableFuture<Message> removeFieldFromMessage(MessageChannel channel, Long messageId, Integer index, Integer embedIndex) {
        return retrieveMessageInChannel(channel, messageId).thenCompose(message -> {
            EmbedBuilder embedBuilder = new EmbedBuilder(message.getEmbeds().get(embedIndex));
            embedBuilder.getFields().remove(index.intValue());
            log.trace("Removing field with index {} from message {}.", index, messageId);
            return editEmbedMessageInAChannel(embedBuilder.build(), channel, messageId);
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
            metricService.incrementCounter(CHANNEL_DELETE_METRIC);
            return textChannelById.delete().submit();
        }
        throw new ChannelNotInGuildException(channelId);
    }

    @Override
    @Transactional
    public List<CompletableFuture<Message>> sendEmbedTemplateInTextChannelList(String templateKey, Object model, TextChannel channel) {
        MessageToSend messageToSend = templateService.renderEmbedTemplate(templateKey, model, channel.getGuild().getIdLong());
        return sendMessageToSendToChannel(messageToSend, channel);
    }

    @Override
    public List<CompletableFuture<Message>> sendEmbedTemplateInMessageChannelList(String templateKey, Object model, MessageChannel channel) {
        // message channel on its own, does not have a guild, so we cant say for which server we want to render the template
        MessageToSend messageToSend = templateService.renderEmbedTemplate(templateKey, model);
        return sendMessageToSendToChannel(messageToSend, channel);
    }

    @Override
    public CompletableFuture<Message> sendTextTemplateInTextChannel(String templateKey, Object model, TextChannel channel) {
        String text = templateService.renderTemplate(templateKey, model, channel.getGuild().getIdLong());
        return sendTextToChannel(text, channel);
    }

    @Override
    public CompletableFuture<Message> sendTextTemplateInMessageChannel(String templateKey, Object model, MessageChannel channel) {
        // message channel on its own, does not have a guild, so we cant say for which server we want to render the template
        String text = templateService.renderTemplate(templateKey, model);
        return sendTextToChannel(text, channel);
    }

    @Override
    public RestAction<Void> deleteMessagesInChannel(TextChannel textChannel, List<Message> messages) {
        metricService.incrementCounter(CHANNEL_MESSAGE_BULK_DELETE_METRIC);
        return textChannel.deleteMessages(messages);
    }

    @Override
    public CompletableFuture<TextChannel> createTextChannel(String name, AServer server, Long categoryId) {
        Optional<Guild> guildById = guildService.getGuildByIdOptional(server.getId());
        if(guildById.isPresent()) {
            Guild guild = guildById.get();
            Category categoryById = guild.getCategoryById(categoryId);
            if(categoryById != null) {
                metricService.incrementCounter(CHANNEL_CREATE_METRIC);
                log.info("Creating channel on server {} in category {}.", server.getId(), categoryById);
                return categoryById.createTextChannel(name).submit();
            }
            throw new CategoryNotFoundException(categoryId, server.getId());
        }
        throw new GuildNotFoundException(server.getId());
    }

    @Override
    public Optional<TextChannel> getChannelFromAChannel(AChannel channel) {
        return getTextChannelFromServerOptional(channel.getServer().getId(), channel.getId());
    }

    @Override
    public AChannel getFakeChannelFromTextChannel(TextChannel textChannel) {
        AServer server = AServer
                .builder()
                .id(textChannel.getGuild().getIdLong())
                .fake(true)
                .build();
        return AChannel
                .builder()
                .fake(true)
                .id(textChannel.getIdLong())
                .server(server)
                .build();
    }

    @Override
    public CompletableFuture<Message> sendSimpleTemplateToChannel(Long serverId, Long channelId, String template) {
        TextChannel textChannel = getTextChannelFromServer(serverId, channelId);
        return sendTextTemplateInTextChannel(template, new Object(), textChannel);
    }

    @Override
    public CompletableFuture<MessageHistory> getHistoryOfChannel(TextChannel channel, Long startMessageId, Integer amount) {
        return channel.getHistoryBefore(startMessageId, amount).submit();
    }

    @Override
    public Optional<TextChannel> getTextChannelFromServerOptional(Guild guild, Long textChannelId) {
        return Optional.ofNullable(guild.getTextChannelById(textChannelId));
    }

    @Override
    public TextChannel getTextChannelFromServer(Guild guild, Long textChannelId) {
        return getTextChannelFromServerOptional(guild, textChannelId).orElseThrow(() -> new ChannelNotInGuildException(textChannelId));
    }

    @Override
    public TextChannel getTextChannelFromServerNullable(Guild guild, Long textChannelId) {
        return getTextChannelFromServerOptional(guild, textChannelId).orElse(null);
    }

    @Override
    public Optional<TextChannel> getTextChannelFromServerOptional(Long serverId, Long textChannelId)  {
        Optional<Guild> guildOptional = guildService.getGuildByIdOptional(serverId);
        if(guildOptional.isPresent()) {
            Guild guild = guildOptional.get();
            return Optional.ofNullable(guild.getTextChannelById(textChannelId));
        }
        throw new GuildNotFoundException(serverId);
    }

    @Override
    public TextChannel getTextChannelFromServer(Long serverId, Long textChannelId) {
        return getTextChannelFromServerOptional(serverId, textChannelId).orElseThrow(() -> new ChannelNotInGuildException(textChannelId));
    }

    @Override
    public CompletableFuture<Void> setSlowModeInChannel(TextChannel textChannel, Integer seconds) {
        metricService.incrementCounter(CHANNEL_CHANGE_SLOW_MODE);
        return textChannel.getManager().setSlowmode(seconds).submit();
    }

    @Override
    public List<CompletableFuture<Message>> sendFileToChannel(String fileContent, String fileNameTemplate, String messageTemplate, Object model, TextChannel channel) {
        String fileName = templateService.renderTemplate(fileNameTemplate, model);
        File tempFile = fileService.createTempFile(fileName);
        try {
            fileService.writeContentToFile(tempFile, fileContent);
            long maxFileSize = channel.getGuild().getMaxFileSize();
            // in this case, we cannot upload the file, so we need to fail
            if(tempFile.length() > maxFileSize) {
                throw new UploadFileTooLargeException(tempFile.length(), maxFileSize);
            }
            MessageToSend messageToSend = templateService.renderEmbedTemplate(messageTemplate, model);
            messageToSend.setFileToSend(tempFile);
            return sendMessageToSendToChannel(messageToSend, channel);
        } catch (IOException e) {
            log.error("Failed to write local temporary file for template download.", e);
            throw new AbstractoRunTimeException(e);
        } finally {
            try {
                fileService.safeDelete(tempFile);
            } catch (IOException e) {
                log.error("Failed to safely delete local temporary file for template download.", e);
            }
        }
    }


    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(CHANNEL_CREATE_METRIC, "Amount of channels created");
        metricService.registerCounter(CHANNEL_DELETE_METRIC, "Amount of channels deleted");
        metricService.registerCounter(CHANNEL_MESSAGE_BULK_DELETE_METRIC, "Amount of channel bulk message delete");
        metricService.registerCounter(CHANNEL_RETRIEVE_HISTORY, "Amount of retrievals of message history in a channel");
        metricService.registerCounter(CHANNEL_CHANGE_SLOW_MODE, "Amount of times slowmode has been changed");
    }
}

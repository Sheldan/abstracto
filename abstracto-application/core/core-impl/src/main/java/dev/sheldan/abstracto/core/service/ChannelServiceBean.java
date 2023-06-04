package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.*;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.AttachedFile;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FileService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

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
        GuildMessageChannel guildMessageChannel = getGuildMessageChannelFromAChannel(channel);
        return sendTextToChannel(text, guildMessageChannel);
    }

    @Override
    public CompletableFuture<Message> sendMessageToAChannel(Message message, AChannel channel) {
        GuildMessageChannel foundChannel = getMessageChannelFromServer(channel.getServer().getId(), channel.getId());
        return sendMessageToChannel(message, foundChannel);
    }

    @Override
    public CompletableFuture<Message> sendMessageToChannel(Message message, GuildMessageChannel channel) {
        log.debug("Sending message {} from channel {} and server {} to channel {}.",
                message.getId(), message.getChannel().getId(), message.getGuild().getId(), channel.getId());
        metricService.incrementCounter(MESSAGE_SEND_METRIC);
        MessageCreateData messageCreateData = MessageCreateData.fromMessage(message);
        return channel.sendMessage(messageCreateData).setAllowedMentions(allowedMentionService.getAllowedMentionsFor(channel, null)).submit();
    }

    @Override
    public CompletableFuture<Message> sendTextToChannel(String text, MessageChannel channel) {
        log.debug("Sending text to channel {}.", channel.getId());
        metricService.incrementCounter(MESSAGE_SEND_METRIC);
        return channel.sendMessage(text).setAllowedMentions(allowedMentionService.getAllowedMentionsFor(channel, null)).submit();
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
        log.debug("Sending embed to channel {}.", channel.getId());
        return sendEmbedToChannelInComplete(embed, channel).submit();
    }

    @Override
    public MessageCreateAction sendEmbedToChannelInComplete(MessageEmbed embed, MessageChannel channel) {
        metricService.incrementCounter(MESSAGE_SEND_METRIC);
        return channel.sendMessageEmbeds(embed).setAllowedMentions(allowedMentionService.getAllowedMentionsFor(channel, null));
    }

    @Override
    public List<CompletableFuture<Message>> sendMessageEmbedToSendToAChannel(MessageToSend messageToSend, AChannel channel) {
        GuildMessageChannel textChannelFromServer = getMessageChannelFromServer(channel.getServer().getId(), channel.getId());
        return sendMessageToSendToChannel(messageToSend, textChannelFromServer);
    }

    @Override
    public CompletableFuture<Message> sendMessageEmbedToSendToAChannel(MessageToSend messageToSend, AChannel channel, Integer embedIndex) {
        return sendEmbedToAChannel(messageToSend.getEmbeds().get(embedIndex), channel);
    }

    @Override
    public CompletableFuture<Message> retrieveMessageInChannel(Long serverId, Long channelId, Long messageId) {
        MessageChannel channel = getMessageChannelFromServer(serverId, channelId);
        return retrieveMessageInChannel(channel, messageId);
    }

    @Override
    public CompletableFuture<Message> retrieveMessageInChannel(MessageChannel channel, Long messageId) {
        metricService.incrementCounter(MESSAGE_LOAD_METRIC);
        return channel.retrieveMessageById(messageId).submit();
    }

    @Override
    public List<CompletableFuture<Message>> sendMessageToSendToChannel(MessageToSend messageToSend, MessageChannel textChannel) {
        if(messageToSend.getEphemeral()) {
            throw new IllegalArgumentException("Ephemeral messages are only supported in interaction context.");
        }
        if(textChannel instanceof GuildMessageChannel) {
            GuildMessageChannel guildMessageChannel = (GuildMessageChannel) textChannel;
            long maxFileSize = guildMessageChannel.getGuild().getMaxFileSize();
            // in this case, we cannot upload the file, so we need to fail
            messageToSend.getAttachedFiles().forEach(attachedFile -> {
                if(attachedFile.getFile().length() > maxFileSize) {
                    throw new UploadFileTooLargeException(attachedFile.getFile().length(), maxFileSize);
                }
            });
        }
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        List<MessageCreateAction> allMessageActions = new ArrayList<>();
        Iterator<MessageEmbed> embedIterator = messageToSend.getEmbeds().iterator();
        for (int i = 0; i < messageToSend.getMessages().size(); i++) {
            metricService.incrementCounter(MESSAGE_SEND_METRIC);
            String text = messageToSend.getMessages().get(i);
            List<MessageEmbed> messageEmbeds = new ArrayList<>();
            while(embedIterator.hasNext()) {
                MessageEmbed embedToAdd = embedIterator.next();
                if((currentEmbedLength(messageEmbeds) + embedToAdd.getLength()) >= MessageEmbed.EMBED_MAX_LENGTH_BOT) {
                    break;
                }
                messageEmbeds.add(embedToAdd);
                embedIterator.remove();
            }
            MessageCreateAction messageAction = textChannel.sendMessage(text);
            if(!messageEmbeds.isEmpty()) {
                messageAction.setEmbeds(messageEmbeds);
            }
            allMessageActions.add(messageAction);
        }
        List<MessageEmbed> messageEmbeds = new ArrayList<>();
        // reset the iterator, because if the if in the above while iterator loop applied, we already took it out from the iterator
        // but we didnt add it yet, so it would be lost
        embedIterator = messageToSend.getEmbeds().iterator();
        while(embedIterator.hasNext()) {
            MessageEmbed embedToAdd = embedIterator.next();
            if((currentEmbedLength(messageEmbeds) + embedToAdd.getLength()) >= MessageEmbed.EMBED_MAX_LENGTH_BOT && !messageEmbeds.isEmpty()) {
                allMessageActions.add(textChannel.sendMessageEmbeds(messageEmbeds));
                metricService.incrementCounter(MESSAGE_SEND_METRIC);
                messageEmbeds = new ArrayList<>();
            }
            messageEmbeds.add(embedToAdd);
        }

        if(!messageEmbeds.isEmpty()) {
            allMessageActions.add(textChannel.sendMessageEmbeds(messageEmbeds));
            metricService.incrementCounter(MESSAGE_SEND_METRIC);
        }

        List<ActionRow> actionRows = messageToSend.getActionRows();
        if(!actionRows.isEmpty()) {
            List<List<ActionRow>> groupedActionRows = ListUtils.partition(actionRows, ComponentService.MAX_BUTTONS_PER_ROW);
            for (int i = 0; i < allMessageActions.size(); i++) {
                allMessageActions.set(i, allMessageActions.get(i).setComponents(groupedActionRows.get(i)));
            }
            for (int i = allMessageActions.size(); i < groupedActionRows.size(); i++) {
                // TODO maybe possible nicer
                allMessageActions.add(textChannel.sendMessage(".").setComponents(groupedActionRows.get(i)));
            }
            AServer server = null;
            if(textChannel instanceof GuildChannel) {
                GuildChannel channel = (GuildChannel) textChannel;
                server = serverManagementService.loadServer(channel.getGuild());
            }
            for (ActionRow components : actionRows) {
                for (ItemComponent component : components) {
                    if (component instanceof ActionComponent) {
                        String id = ((ActionComponent) component).getId();
                        MessageToSend.ComponentConfig payload = messageToSend.getComponentPayloads().get(id);
                        if (payload != null && payload.getPersistCallback()) {
                            componentPayloadManagementService.createPayload(id, payload.getPayload(), payload.getPayloadType(), payload.getComponentOrigin(), server, payload.getComponentType());
                        }
                    }
                }
            }
        }

        if(messageToSend.hasFilesToSend()) {
            List<FileUpload> attachedFiles = messageToSend
                    .getAttachedFiles()
                    .stream()
                    .map(AttachedFile::convertToFileUpload)
                    .collect(Collectors.toList());
            if(!allMessageActions.isEmpty()) {
                // in case there has not been a message, we need to increment it
                allMessageActions.set(0, allMessageActions.get(0).addFiles(attachedFiles));
            } else {
                metricService.incrementCounter(MESSAGE_SEND_METRIC);
                allMessageActions.add(textChannel.sendFiles(attachedFiles));
            }
        }
        Set<Message.MentionType> allowedMentions = allowedMentionService.getAllowedMentionsFor(textChannel, messageToSend);
        allMessageActions.forEach(messageAction -> {
            if(messageToSend.getReferencedMessageId() != null) {
                messageAction = messageAction.setMessageReference(messageToSend.getReferencedMessageId());
                if(messageToSend.getMessageConfig() != null && !messageToSend.getMessageConfig().isMentionsReferencedMessage()) {
                    messageAction = messageAction.mentionRepliedUser(false);
                }
            }
            futures.add(messageAction.setAllowedMentions(allowedMentions).submit());
        });
        return futures;
    }

    private Integer currentEmbedLength(List<MessageEmbed> messageEmbeds) {
        return messageEmbeds.stream().mapToInt(MessageEmbed::getLength).sum();
    }

    @Override
    public void editMessageInAChannel(MessageToSend messageToSend, AChannel channel, Long messageId) {
        Optional<GuildChannel> textChannelFromServer = getGuildChannelFromServerOptional(channel.getServer().getId(), channel.getId());
        if(textChannelFromServer.isPresent() && textChannelFromServer.get() instanceof GuildMessageChannel) {
            GuildMessageChannel messageChannel = (GuildMessageChannel) textChannelFromServer.get();
            editMessageInAChannel(messageToSend, messageChannel, messageId);
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
        MessageEditAction messageAction;
        if(!messageToSend.getMessages().isEmpty() && !StringUtils.isBlank(messageToSend.getMessages().get(0))) {
            log.debug("Editing message {} with new text content.", messageId);
            messageAction = channel.editMessageById(messageId, messageToSend.getMessages().get(0));
            if(messageToSend.getEmbeds() != null && !messageToSend.getEmbeds().isEmpty()) {
                log.debug("Also editing the embed for message {}.", messageId);
                messageAction = messageAction.setEmbeds(messageToSend.getEmbeds());
            }
        } else {
            log.debug("Editing message {} with new embeds.", messageId);
            if(messageToSend.getEmbeds() != null && !messageToSend.getEmbeds().isEmpty()) {
                messageAction = channel.editMessageEmbedsById(messageId, messageToSend.getEmbeds());
            } else {
                throw new IllegalArgumentException("Message to send did not contain anything to send.");
            }
        }
        if(!messageToSend.getAttachedFiles().isEmpty()) {
            List<FileUpload> files = messageToSend
                    .getAttachedFiles()
                    .stream()
                    .map(AttachedFile::convertToFileUpload)
                    .collect(Collectors.toList());
            messageAction = messageAction.setFiles(files);
        }
        messageAction = messageAction.setComponents(messageToSend.getActionRows()).setReplace(true);
        metricService.incrementCounter(MESSAGE_EDIT_METRIC);
        return messageAction.submit();
    }

    @Override
    public CompletableFuture<Message> editEmbedMessageInAChannel(MessageEmbed embedToSend, MessageChannel channel, Long messageId) {
        metricService.incrementCounter(MESSAGE_EDIT_METRIC);
        return channel.editMessageEmbedsById(messageId, embedToSend).submit();
    }

    @Override
    public CompletableFuture<Message> editTextMessageInAChannel(String text, MessageChannel channel, Long messageId) {
        metricService.incrementCounter(MESSAGE_EDIT_METRIC);
        return channel.editMessageById(messageId, text).submit();
    }

    @Override
    public CompletableFuture<Message> editTextMessageInAChannel(String text, MessageEmbed messageEmbed, MessageChannel channel, Long messageId) {
        metricService.incrementCounter(MESSAGE_EDIT_METRIC);
        return channel.editMessageById(messageId, text).setEmbeds(messageEmbed).submit();
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
    public CompletableFuture<Message> editFieldValueInMessage(MessageChannel channel, Long messageId, Integer index, String newValue) {
        return retrieveMessageInChannel(channel, messageId).thenCompose(message -> {
            EmbedBuilder embedBuilder = new EmbedBuilder(message.getEmbeds().get(index));
            MessageEmbed.Field existingField = embedBuilder.getFields().get(index);
            MessageEmbed.Field newField = new MessageEmbed.Field(existingField.getName(), newValue, existingField.isInline());
            embedBuilder.getFields().set(index, newField);
            log.debug("Updating field with index {} from message {}.", index, messageId);
            return editEmbedMessageInAChannel(embedBuilder.build(), channel, messageId);
        });
    }

    @Override
    public CompletableFuture<Message> removeFieldFromMessage(MessageChannel channel, Long messageId, Integer index, Integer embedIndex) {
        return retrieveMessageInChannel(channel, messageId).thenCompose(message -> {
            EmbedBuilder embedBuilder = new EmbedBuilder(message.getEmbeds().get(embedIndex));
            embedBuilder.getFields().remove(index.intValue());
            log.debug("Removing field with index {} from message {}.", index, messageId);
            return editEmbedMessageInAChannel(embedBuilder.build(), channel, messageId);
        });
    }

    @Override
    public CompletableFuture<Message> removeComponents(MessageChannel channel, Long messageId) {
        return channel.editMessageComponentsById(messageId, new ArrayList<>()).submit();
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
    public List<CompletableFuture<Message>> sendEmbedTemplateInTextChannelList(String templateKey, Object model, MessageChannel channel) {
        MessageToSend messageToSend;
        if(channel instanceof GuildChannel) {
            messageToSend = templateService.renderEmbedTemplate(templateKey, model, ((GuildChannel)channel).getGuild().getIdLong());
        } else {
            messageToSend = templateService.renderEmbedTemplate(templateKey, model);
        }
        return sendMessageToSendToChannel(messageToSend, channel);
    }

    @Override
    public List<CompletableFuture<Message>> sendEmbedTemplateInMessageChannelList(String templateKey, Object model, MessageChannel channel) {
        // message channel on its own, does not have a guild, so we cant say for which server we want to render the template
        MessageToSend messageToSend = templateService.renderEmbedTemplate(templateKey, model);
        return sendMessageToSendToChannel(messageToSend, channel);
    }

    @Override
    public CompletableFuture<Message> sendTextTemplateInTextChannel(String templateKey, Object model, MessageChannel channel) {
        String text;
        if(channel instanceof GuildChannel) {
            text = templateService.renderTemplate(templateKey, model, ((GuildChannel)channel).getGuild().getIdLong());
        } else {
            text = templateService.renderTemplate(templateKey, model);
        }

        return sendTextToChannel(text, channel);
    }

    @Override
    public CompletableFuture<Message> sendTextTemplateInMessageChannel(String templateKey, Object model, MessageChannel channel) {
        // message channel on its own, does not have a guild, so we cant say for which server we want to render the template
        String text = templateService.renderTemplate(templateKey, model);
        return sendTextToChannel(text, channel);
    }

    @Override
    public CompletableFuture<Void> deleteMessagesInChannel(MessageChannel messageChannel, List<Message> messages) {
        metricService.incrementCounter(CHANNEL_MESSAGE_BULK_DELETE_METRIC);
        List<CompletableFuture<Void>> deleteFutures = messages
                .stream()
                .map(ISnowflake::getId)
                .map(messageId -> messageChannel.deleteMessageById(messageId).submit())
                .collect(Collectors.toList());
        return new CompletableFutureList<>(deleteFutures).getMainFuture();
    }

    @Override
    public CompletableFuture<TextChannel> createTextChannel(String name, AServer server, Long categoryId) {
        Optional<Guild> guildById = guildService.getGuildByIdOptional(server.getId());
        if(guildById.isPresent()) {
            Guild guild = guildById.get();
            Category categoryById = guild.getCategoryById(categoryId);
            if(categoryById != null) {
                metricService.incrementCounter(CHANNEL_CREATE_METRIC);
                log.info("Creating channel on server {} in category {}.", server.getId(), categoryId);
                return categoryById.createTextChannel(name).submit();
            }
            throw new CategoryNotFoundException(categoryId, server.getId());
        }
        throw new GuildNotFoundException(server.getId());
    }

    @Override
    public Optional<GuildChannel> getChannelFromAChannel(AChannel channel) {
        return getGuildChannelFromServerOptional(channel.getServer().getId(), channel.getId());
    }

    @Override
    public Optional<GuildMessageChannel> getGuildMessageChannelFromAChannelOptional(AChannel channel) {
        return getMessageChannelFromServerOptional(channel.getServer().getId(), channel.getId());
    }

    @Override
    public GuildMessageChannel getGuildMessageChannelFromAChannel(AChannel channel) {
        return getMessageChannelFromServer(channel.getServer().getId(), channel.getId());
    }

    @Override
    public AChannel getFakeChannelFromTextChannel(MessageChannel messageChannel) {
        AServer server = null;
        if(messageChannel instanceof GuildChannel) {
            server = AServer
                    .builder()
                    .id(((GuildChannel) messageChannel).getGuild().getIdLong())
                    .fake(true)
                    .build();
        }
        return AChannel
                .builder()
                .fake(true)
                .id(messageChannel.getIdLong())
                .server(server)
                .build();
    }

    @Override
    public CompletableFuture<Message> sendSimpleTemplateToChannel(Long serverId, Long channelId, String template) {
        GuildMessageChannel foundChannel = getMessageChannelFromServer(serverId, channelId);
        if(foundChannel != null) {
            return sendTextTemplateInTextChannel(template, new Object(), foundChannel);
        } else {
            log.info("Channel {} in server {} not found.", channelId, serverId);
            throw new IllegalArgumentException("Incorrect channel type.");
        }
    }

    @Override
    public CompletableFuture<MessageHistory> getHistoryOfChannel(MessageChannel channel, Long startMessageId, Integer amount) {
        return channel.getHistoryBefore(startMessageId, amount).submit();
    }

    @Override
    public Optional<GuildChannel> getGuildChannelFromServerOptional(Guild guild, Long textChannelId) {
        return Optional.ofNullable(guild.getGuildChannelById(textChannelId));
    }

    @Override
    public GuildMessageChannel getMessageChannelFromServer(Guild guild, Long textChannelId) {
        GuildChannel foundChannel = getGuildChannelFromServerOptional(guild, textChannelId).orElseThrow(() -> new ChannelNotInGuildException(textChannelId));
        if(foundChannel instanceof GuildMessageChannel) {
            return (GuildMessageChannel) foundChannel;
        }
        log.info("Incorrect channel type of channel {} in guild {}: {}", textChannelId, guild.getId(), foundChannel.getType());
        throw new IllegalArgumentException("Incorrect channel type found.");
    }

    @Override
    public GuildMessageChannel getMessageChannelFromServer(Long serverId, Long textChannelId) {
        Optional<Guild> guildOptional = guildService.getGuildByIdOptional(serverId);
        if(guildOptional.isPresent()) {
            Guild guild = guildOptional.get();
            return getMessageChannelFromServer(guild, textChannelId);
        }
        throw new GuildNotFoundException(serverId);
    }

    @Override
    public Optional<GuildMessageChannel> getMessageChannelFromServerOptional(Long serverId, Long textChannelId) {
        Optional<GuildChannel> guildChannel = getGuildChannelFromServerOptional(serverId, textChannelId);
        if(guildChannel.isPresent() && guildChannel.get() instanceof GuildMessageChannel) {
            return Optional.of((GuildMessageChannel)guildChannel.get());
        }
        return Optional.empty();
    }

    @Override
    public GuildMessageChannel getMessageChannelFromServerNullable(Guild guild, Long textChannelId) {
        return getMessageChannelFromServer(guild, textChannelId);
    }

    @Override
    public Optional<GuildChannel> getGuildChannelFromServerOptional(Long serverId, Long channelId)  {
        Optional<Guild> guildOptional = guildService.getGuildByIdOptional(serverId);
        if(guildOptional.isPresent()) {
            Guild guild = guildOptional.get();
            return Optional.ofNullable(guild.getGuildChannelById(channelId));
        }
        throw new GuildNotFoundException(serverId);
    }

    @Override
    public GuildChannel getGuildChannelFromServer(Long serverId, Long channelId)  {
        Optional<Guild> guildOptional = guildService.getGuildByIdOptional(serverId);
        if(guildOptional.isPresent()) {
            Guild guild = guildOptional.get();
            return guild.getGuildChannelById(channelId);
        }
        throw new GuildNotFoundException(serverId);
    }

    @Override
    public Optional<TextChannel> getTextChannelFromServerOptional(Long serverId, Long textChannelId) {
        Optional<Guild> guildOptional = guildService.getGuildByIdOptional(serverId);
        if(guildOptional.isPresent()) {
            Guild guild = guildOptional.get();
            return Optional.ofNullable(guild.getTextChannelById(textChannelId));
        }
        throw new GuildNotFoundException(serverId);
    }


    @Override
    public CompletableFuture<Void> setSlowModeInChannel(TextChannel textChannel, Integer seconds) {
        metricService.incrementCounter(CHANNEL_CHANGE_SLOW_MODE);
        return textChannel.getManager().setSlowmode(seconds).submit();
    }

    @Override
    public List<CompletableFuture<Message>> sendFileToChannel(String fileContent, String fileNameTemplate, String messageTemplate, Object model, MessageChannel channel) {
        String fileName = templateService.renderTemplate(fileNameTemplate, model);
        File tempFile = fileService.createTempFile(fileName);
        try {
            fileService.writeContentToFile(tempFile, fileContent);
            MessageToSend messageToSend;
            if(channel instanceof GuildMessageChannel) {
                GuildMessageChannel guildChannel = (GuildMessageChannel) channel;
                long maxFileSize = guildChannel.getGuild().getMaxFileSize();
                // in this case, we cannot upload the file, so we need to fail
                if(tempFile.length() > maxFileSize) {
                    throw new UploadFileTooLargeException(tempFile.length(), maxFileSize);
                }
                messageToSend = templateService.renderEmbedTemplate(messageTemplate, model, guildChannel.getGuild().getIdLong());
            } else {
                messageToSend = templateService.renderEmbedTemplate(messageTemplate, model);
            }
            AttachedFile file = AttachedFile
                    .builder()
                    .file(tempFile)
                    .fileName(fileName)
                    .build();
            messageToSend.setAttachedFiles(Arrays.asList(file));
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

    @Override
    public List<CompletableFuture<Message>> sendFileToChannel(String fileContent, String fileName, MessageChannel channel) {
        File tempFile = fileService.createTempFile(fileName);
        try {
            fileService.writeContentToFile(tempFile, fileContent);
            if(channel instanceof GuildMessageChannel) {
                long maxFileSize = ((GuildMessageChannel) channel).getGuild().getMaxFileSize();
                // in this case, we cannot upload the file, so we need to fail
                if(tempFile.length() > maxFileSize) {
                    throw new UploadFileTooLargeException(tempFile.length(), maxFileSize);
                }
            }
            AttachedFile attachedFile = AttachedFile
                    .builder()
                    .fileName(tempFile.getName())
                    .file(tempFile)
                    .build();
            MessageToSend messageToSend = MessageToSend
                    .builder()
                    .attachedFiles(Arrays.asList(attachedFile))
                    .build();
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

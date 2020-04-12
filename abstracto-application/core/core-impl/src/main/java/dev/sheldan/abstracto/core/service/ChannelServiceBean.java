package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.exception.ChannelException;
import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.models.database.AChannel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class ChannelServiceBean implements ChannelService {

    @Autowired
    private BotService botService;

    @Override
    public void sendTextInAChannel(String text, AChannel channel) {
        sendTextInAChannelFuture(text, channel);
    }

    @Override
    public CompletableFuture<Message> sendTextInAChannelFuture(String text, AChannel channel) {
        Guild guild = botService.getInstance().getGuildById(channel.getServer().getId());
        if (guild != null) {
            TextChannel textChannel = guild.getTextChannelById(channel.getId());
            if(textChannel != null) {
                return textChannel.sendMessage(text).submit();
            } else {
                log.error("Channel {} to post towards was not found in server {}", channel.getId(), channel.getServer().getId());
                throw new ChannelException(String.format("Channel %s to post to not found.", channel.getId()));
            }
        } else {
            log.error("Guild {} was not found when trying to post a message", channel.getServer().getId());
            throw new GuildException(String.format("Guild %s to post in channel %s was not found.", channel.getServer().getId(), channel.getId()));
        }
    }

    @Override
    public CompletableFuture<Message> sendEmbedInAChannelFuture(MessageEmbed embed, AChannel channel) {
        Guild guild = botService.getInstance().getGuildById(channel.getServer().getId());
        if (guild != null) {
            TextChannel textChannel = guild.getTextChannelById(channel.getId());
            if(textChannel != null) {
                return sendEmbedInAChannelFuture(embed, textChannel);
            } else {
                log.error("Channel {} to post towards was not found in server {}", channel.getId(), channel.getServer().getId());
                throw new ChannelException(String.format("Channel %s to post to not found.", channel.getId()));
            }
        } else {
            log.error("Guild {} was not found when trying to post a message", channel.getServer().getId());
            throw new GuildException(String.format("Guild %s to post in channel %s was not found.", channel.getServer().getId(), channel.getId()));
        }
    }

    @Override
    public CompletableFuture<Message> sendEmbedInAChannelFuture(MessageEmbed embed, TextChannel channel) {
        return channel.sendMessage(embed).submit();
    }

    @Override
    public List<CompletableFuture<Message>> sendMessageToEndInAChannel(MessageToSend messageToSend, AChannel channel) {
        Optional<TextChannel> textChannelFromServer = botService.getTextChannelFromServer(channel.getServer().getId(), channel.getId());
        if(textChannelFromServer.isPresent()) {
            return sendMessageToEndInTextChannel(messageToSend, textChannelFromServer.get());
        }
        throw new ChannelException(String.format("Channel %s was not found.", channel.getId()));
    }

    @Override
    public List<CompletableFuture<Message>> sendMessageToEndInTextChannel(MessageToSend messageToSend, TextChannel textChannel) {
        String messageText = messageToSend.getMessage();
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        if(StringUtils.isBlank(messageText)) {
            messageToSend.getEmbeds().forEach(embed -> {
                futures.add(sendEmbedInAChannelFuture(embed, textChannel));
            });
        } else  {
            MessageAction messageAction = textChannel.sendMessage(messageText);
            if(messageToSend.getEmbeds() != null && messageToSend.getEmbeds().size() > 0) {
                CompletableFuture<Message> messageFuture = messageAction.embed(messageToSend.getEmbeds().get(0)).submit();
                futures.add(messageFuture);
                messageToSend.getEmbeds().stream().skip(1).forEach(embed -> {
                    futures.add(sendEmbedInAChannelFuture(embed, textChannel));
                });
            } else {
                futures.add(messageAction.submit());
            }
        }
        return futures;
    }

    @Override
    public Optional<TextChannel> getTextChannelInGuild(Long serverId, Long channelId) {
        return botService.getTextChannelFromServer(serverId, channelId);
    }

    @Override
    public void editMessageInAChannel(MessageToSend messageToSend, AChannel channel, Long messageId) {
        Optional<TextChannel> textChannelFromServer = botService.getTextChannelFromServer(channel.getServer().getId(), channel.getId());
        if(textChannelFromServer.isPresent()) {
            TextChannel textChannel = textChannelFromServer.get();
            editMessageInAChannel(messageToSend, textChannel, messageId);
        } else {
            throw new ChannelException(String.format("Channel %s was not found.", channel.getId()));
        }
    }

    @Override
    public void editMessageInAChannel(MessageToSend messageToSend, TextChannel channel, Long messageId) {
        MessageAction messageAction;
        if(!StringUtils.isBlank(messageToSend.getMessage())) {
            messageAction = channel.editMessageById(messageId, messageToSend.getMessage());
            if(messageToSend.getEmbeds() != null && messageToSend.getEmbeds().size() > 0) {
                messageAction = messageAction.embed(messageToSend.getEmbeds().get(0));
            }
        } else {
            if(messageToSend.getEmbeds() != null && messageToSend.getEmbeds().size() > 0) {
                messageAction = channel.editMessageById(messageId, messageToSend.getEmbeds().get(0));
            } else {
                throw new AbstractoRunTimeException("Message to send did not contain anything to send.");
            }
        }
        messageAction.queue();
    }
}

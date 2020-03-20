package dev.sheldan.abstracto.core.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component @Slf4j
@CacheConfig(cacheNames = {"messages"})
public class MessageCacheBean implements MessageCache {

    @Autowired
    private Bot bot;

    @Override
    @CachePut(key = "#message.id")
    public Message putMessageInCache(Message message) {
        log.debug("Adding message {} to cache", message.getId());
        return message;
    }

    @Override
    @Cacheable(key = "#message.id")
    public Message getMessageFromCache(Message message) {
        log.debug("Retrieving message {}", message.getId());
        return getMessageInTextChannelOfGuild(message.getIdLong(), message.getTextChannel().getIdLong(), message.getGuild().getIdLong());
    }

    @Override
    @Cacheable(key = "#messageId.toString()")
    public Message getMessageFromCache(Long messageId, Long textChannelId, Long guildId) {
        log.info("Retrieving message with parameters");
        return getMessageInTextChannelOfGuild(messageId, textChannelId, guildId);
    }

    private Message getMessageInTextChannelOfGuild(Long messageId, Long textChannelId, Long guildId) {
        Guild guildById = bot.getInstance().getGuildById(guildId);
        if(guildById != null) {
            TextChannel textChannelById = guildById.getTextChannelById(textChannelId);
            if(textChannelById != null) {
                return textChannelById.retrieveMessageById(messageId).complete();
            } else {
                log.warn("Failed to load text channel {} of message {} in guild {}", textChannelId, messageId, guildId);
            }
        } else {
            log.warn("Failed to guild {} of message {}", guildId, messageId);
        }
        throw new RuntimeException("Message was not found");
    }

}

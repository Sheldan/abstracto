package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.ChannelNotInGuildException;
import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@CacheConfig(cacheNames = "messages")
public class MessageCacheBean implements MessageCache {

    @Autowired
    private GuildService guildService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private CacheEntityService cacheEntityService;

    @Autowired
    @Lazy
    // needs to be lazy, because of circular dependency
    private MessageCacheBean concreteSelf;

    @Override
    @CachePut(key = "#message.id")
    public CompletableFuture<CachedMessage> putMessageInCache(Message message) {
        log.debug("Adding message {} to cache.", message.getId());
        return cacheEntityService.buildCachedMessageFromMessage(message);
    }


    @Override
    @CachePut(key = "#message.messageId.toString()")
    public CompletableFuture<CachedMessage> putMessageInCache(CachedMessage message) {
        log.debug("Adding cached message {} to cache.", message.getMessageId());
        return CompletableFuture.completedFuture(message);
    }

    @Override
    @Cacheable(key = "#message.id")
    public CompletableFuture<CachedMessage> getMessageFromCache(Message message) {
        log.debug("Retrieving message {}.", message.getId());
        return getMessageFromCache(message.getGuild().getIdLong(), message.getChannel().getIdLong(), message.getIdLong());
    }

    @Override
    @Cacheable(key = "#messageId.toString()")
    public CompletableFuture<CachedMessage> getMessageFromCache(Long guildId, Long textChannelId, Long messageId) {
        log.debug("Retrieving message {}.", messageId);
        return concreteSelf.loadMessage(guildId, textChannelId, messageId);
    }

    @Override
    public CompletableFuture<CachedMessage> loadMessage(Long guildId, Long textChannelId, Long messageId) {
        log.debug("Loading message {} from channel {} in server {}.", messageId, textChannelId, guildId);
        CompletableFuture<CachedMessage> future = new CompletableFuture<>();
        Optional<Guild> guildOptional = guildService.getGuildByIdOptional(guildId);
        if(guildOptional.isPresent()) {
            Optional<GuildMessageChannel> textChannelByIdOptional = channelService.getMessageChannelFromServerOptional(guildId, textChannelId);
            if(textChannelByIdOptional.isPresent()) {
                MessageChannel messageChannel = textChannelByIdOptional.get();
                channelService.retrieveMessageInChannel(messageChannel, messageId)
                        .thenAccept(message ->
                            cacheEntityService.buildCachedMessageFromMessage(message)
                                    .thenAccept(future::complete)
                                    .exceptionally(throwable -> {
                                        log.error("Failed to load message for caching.", throwable);
                                        future.completeExceptionally(throwable);
                                        return null;
                                    })
                        ).exceptionally(throwable -> {
                            log.error("Failed to load message for caching.", throwable);
                            return null;
                        });
            } else {
                log.error("Not able to load message {} in channel {} in guild {}. Text channel not found.", messageId, textChannelId, guildId);
                future.completeExceptionally(new ChannelNotInGuildException(textChannelId));
            }
        } else {
            log.error("Not able to load message {} in channel {} in guild {}. Guild not found.", messageId, textChannelId, guildId);
            future.completeExceptionally(new GuildNotFoundException(guildId));
        }

        return future;
    }

    @Override
    @CacheEvict(allEntries = true)
    public void clearCache() {
        log.info("Clearing message cache.");
    }


}

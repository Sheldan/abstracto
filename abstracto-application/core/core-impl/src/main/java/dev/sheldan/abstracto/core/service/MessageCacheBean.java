package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import dev.sheldan.abstracto.core.models.cache.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
@CacheConfig(cacheNames = "messages")
public class MessageCacheBean implements MessageCache {

    @Autowired
    private BotService botService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    @Lazy
    // needs to be lazy, because of circular dependency
    private MessageCache self;

    @Autowired
    @Lazy
    // needs to be lazy, because of circular dependency
    private MessageCacheBean concreteSelf;

    @Override
    @CachePut(key = "#message.id")
    public CompletableFuture<CachedMessage> putMessageInCache(Message message) {
        log.info("Adding message {} to cache", message.getId());
        return self.buildCachedMessageFromMessage(message);
    }


    @Override
    @CachePut(key = "#message.messageId.toString()")
    public CompletableFuture<CachedMessage> putMessageInCache(CachedMessage message) {
        log.info("Adding cached message to cache");
        return CompletableFuture.completedFuture(message);
    }

    @Override
    @Cacheable(key = "#message.id")
    public CompletableFuture<CachedMessage> getMessageFromCache(Message message) {
        log.info("Retrieving message {}", message.getId());
        return getMessageFromCache(message.getGuild().getIdLong(), message.getChannel().getIdLong(), message.getIdLong());
    }

    @Override
    @Cacheable(key = "#messageId.toString()")
    public CompletableFuture<CachedMessage> getMessageFromCache(Long guildId, Long textChannelId, Long messageId) {
        log.info("Retrieving message with parameters");

        return self.loadMessage(guildId, textChannelId, messageId);
    }

    @Override
    public CompletableFuture<CachedMessage> loadMessage(Long guildId, Long textChannelId, Long messageId) {
        CompletableFuture<CachedMessage> future = new CompletableFuture<>();
        Optional<Guild> guildOptional = botService.getGuildById(guildId);
        if(guildOptional.isPresent()) {
            Optional<TextChannel> textChannelByIdOptional = botService.getTextChannelFromServer(guildOptional.get(), textChannelId);
            if(textChannelByIdOptional.isPresent()) {
                TextChannel textChannel = textChannelByIdOptional.get();
                textChannel.retrieveMessageById(messageId).queue(message ->

                            buildCachedMessageFromMessage(message)
                                    .thenAccept(future::complete)
                                    .exceptionally(throwable -> {
                                        log.error("Failed to load message for caching.", throwable);
                                        future.completeExceptionally(throwable);
                                        return null;
                                    })

                );
            } else {
                log.error("Not able to load message {} in channel {} in guild {}. Text channel not found.", messageId, textChannelId, guildId);
                future.completeExceptionally(new ChannelNotFoundException(textChannelId, guildId));
            }
        } else {
            log.error("Not able to load message {} in channel {} in guild {}. Guild not found.", messageId, textChannelId, guildId);
            future.completeExceptionally(new GuildException(guildId));
        }

        return future;
    }

    @Override
    public CompletableFuture<CachedMessage> buildCachedMessageFromMessage(Message message) {
        CompletableFuture<CachedMessage> future = new CompletableFuture<>();
        List<String> attachmentUrls = new ArrayList<>();
        message.getAttachments().forEach(attachment ->
            attachmentUrls.add(attachment.getProxyUrl())
        );
        List<CachedEmbed> embeds = new ArrayList<>();
        message.getEmbeds().forEach(embed ->
            embeds.add(getCachedEmbedFromEmbed(embed))
        );

        List<CompletableFuture<CachedReaction>> futures = new ArrayList<>();
        message.getReactions().forEach(messageReaction -> futures.add(self.getCachedReactionFromReaction(messageReaction)));

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(aVoid ->
        future.complete(CachedMessage.builder()
                    .authorId(message.getAuthor().getIdLong())
                    .serverId(message.getGuild().getIdLong())
                    .messageId(message.getIdLong())
                    .channelId(message.getChannel().getIdLong())
                    .content(message.getContentRaw())
                    .embeds(embeds)
                    .reactions(getFutures(futures))
                    .timeCreated(Instant.from(message.getTimeCreated()))
                    .attachmentUrls(attachmentUrls)
                    .build())
        ).exceptionally(throwable -> {
            log.error("Failed to load reactions for message {}. ", message.getId(), throwable);
            return null;
        });
        return future;
    }

    private List<CachedReaction> getFutures(List<CompletableFuture<CachedReaction>> futures) {
        List<CachedReaction> reactions = new ArrayList<>();
        futures.forEach(future -> {
            try {
                CachedReaction cachedReaction = future.get();
                reactions.add(cachedReaction);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error while executing future to retrieve reaction.", e);
                Thread.currentThread().interrupt();
            }
        });
        return reactions;
    }

    @Override
    public CompletableFuture<CachedReaction> getCachedReactionFromReaction(MessageReaction reaction) {
        CompletableFuture<CachedReaction> future = new CompletableFuture<>();
        ReactionPaginationAction users = reaction.retrieveUsers().cache(false);
        CachedReaction.CachedReactionBuilder builder = CachedReaction.builder();

        List<Long> aUsers = new ArrayList<>();
        users.forEachAsync(user -> {
            concreteSelf.loadUser(reaction, aUsers, user);
            return false;
        }).thenAccept(o -> future.complete(builder.build()))
        .exceptionally(throwable -> {
            log.error("Failed to load reaction users.", throwable);
            return null;
        });
        builder.userInServersIds(aUsers);
        builder.emote(emoteService.buildAEmoteFromReaction(reaction.getReactionEmote()));
        return future;
    }

    @Transactional
    public void loadUser(MessageReaction reaction, List<Long> aUsers, User user) {
        if(reaction.getGuild() != null) {
            aUsers.add(userInServerManagementService.loadUser(reaction.getGuild().getIdLong(), user.getIdLong()).getUserInServerId());
        }
    }

    private CachedEmbed getCachedEmbedFromEmbed(MessageEmbed embed) {
        CachedEmbed.CachedEmbedBuilder builder = CachedEmbed
                .builder();
        MessageEmbed.AuthorInfo author = embed.getAuthor();
        if(author != null) {
            builder.author(CachedEmbedAuthor.builder().avatar(author.getProxyIconUrl()).url(author.getUrl()).name(author.getName()).build());
        }
        List<MessageEmbed.Field> fields = embed.getFields();
        if(!fields.isEmpty()) {
            List<CachedEmbedField> cachedEmbedFields = new ArrayList<>();
            fields.forEach(field -> {
               CachedEmbedField build = CachedEmbedField
                       .builder()
                       .name(field.getName())
                       .value(field.getValue())
                       .inline(field.isInline())
                       .build();
               cachedEmbedFields.add(build);
            });
            builder.fields(cachedEmbedFields);
        }
        MessageEmbed.ImageInfo image = embed.getImage();
        if(image != null) {
            builder.imageUrl(image.getProxyUrl());
        }
        Color color = embed.getColor();
        if(color != null) {
            CachedEmbedColor build = CachedEmbedColor
                    .builder()
                    .r(color.getRed())
                    .g(color.getGreen())
                    .b(color.getBlue())
                    .build();
            builder.color(build);
        }
        builder.description(embed.getDescription());
        MessageEmbed.Footer footer = embed.getFooter();
        if(footer != null) {
            CachedEmbedFooter build = CachedEmbedFooter
                    .builder()
                    .icon(footer.getProxyIconUrl())
                    .text(footer.getText())
                    .build();
            builder.footer(build);
        }

        return builder.build();
    }

}

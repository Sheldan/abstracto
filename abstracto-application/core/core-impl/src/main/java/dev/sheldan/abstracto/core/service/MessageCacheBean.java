package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.ChannelException;
import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.cache.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class MessageCacheBean implements MessageCache {

    @Autowired
    private BotService botService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    @Lazy
    // needs to be lazy, because of circular dependency
    private MessageCache self;

    @Override
    @CachePut(key = "#message.id", cacheNames = "messages")
    public CompletableFuture<CachedMessage> putMessageInCache(Message message) {
        log.info("Adding message {} to cache", message.getId());
        CompletableFuture<CachedMessage> future = new CompletableFuture<>();
        self.buildCachedMessageFromMessage(future, message);
        return future;
    }


    @CachePut(key = "#message.messageId")
    public CompletableFuture<CachedMessage> putMessageInCache(CachedMessage message) {
        log.info("Adding cached message to cache");
        return CompletableFuture.completedFuture(message);
    }

    @Override
    @Cacheable(key = "#message.id", cacheNames = "messages")
    public CompletableFuture<CachedMessage> getMessageFromCache(Message message) {
        log.info("Retrieving message {}", message.getId());
        return getMessageFromCache(message.getGuild().getIdLong(), message.getChannel().getIdLong(), message.getIdLong());
    }

    @Override
    @Cacheable(key = "#messageId.toString()", cacheNames = "messages")
    public CompletableFuture<CachedMessage> getMessageFromCache(Long guildId, Long textChannelId, Long messageId) {
        log.info("Retrieving message with parameters");

        CompletableFuture<CachedMessage> cachedMessageCompletableFuture = new CompletableFuture<>();
        self.loadMessage(cachedMessageCompletableFuture, guildId, textChannelId, messageId);
        return cachedMessageCompletableFuture;
    }

    @Async
    @Override
    public void loadMessage(CompletableFuture<CachedMessage> future, Long guildId, Long textChannelId, Long messageId) {
        Optional<Guild> guildOptional = botService.getGuildById(guildId);
        if(guildOptional.isPresent()) {
            Optional<TextChannel> textChannelByIdOptional = botService.getTextChannelFromServer(guildOptional.get(), textChannelId);
            if(textChannelByIdOptional.isPresent()) {
                TextChannel textChannel = textChannelByIdOptional.get();
                textChannel.retrieveMessageById(messageId).queue(message -> {
                    buildCachedMessageFromMessage(future, message);
                });
            } else {
                log.error("Not able to load message {} in channel {} in guild {}. Text channel not found.", messageId, textChannelId, guildId);
                future.completeExceptionally(new ChannelException(String.format("Not able to load message %s. Text channel %s not found in guild %s", messageId, textChannelId, guildId)));
            }
        } else {
            log.error("Not able to load message {} in channel {} in guild {}. Guild not found.", messageId, textChannelId, guildId);
            future.completeExceptionally(new GuildException(String.format("Not able to load message %s. Guild %s not found.", messageId, guildId)));

        }
    }

    @Override
    @Async
    public void buildCachedMessageFromMessage(CompletableFuture<CachedMessage> future, Message message) {
        List<String> attachmentUrls = new ArrayList<>();
        message.getAttachments().forEach(attachment -> {
            attachmentUrls.add(attachment.getProxyUrl());
        });
        List<CachedEmbed> embeds = new ArrayList<>();
        message.getEmbeds().forEach(embed -> {
            embeds.add(getCachedEmbedFromEmbed(embed));
        });

        List<CompletableFuture<CachedReaction>> futures = new ArrayList<>();
        message.getReactions().forEach(messageReaction -> {
            CompletableFuture<CachedReaction> future1 = new CompletableFuture<>();
            self.getCachedReactionFromReaction(future1, messageReaction);
            futures.add(future1);
        });

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
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
                    .build());
        });
    }

    private List<CachedReaction> getFutures(List<CompletableFuture<CachedReaction>> futures) {
        List<CachedReaction> reactions = new ArrayList<>();
        futures.forEach(future -> {
            try {
                CachedReaction cachedReaction = future.get();
                reactions.add(cachedReaction);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error while executing future to retrieve reaction.", e);
            }
        });
        return reactions;
    }

    @Override
    @Async
    public void getCachedReactionFromReaction(CompletableFuture<CachedReaction> future, MessageReaction reaction) {
        ReactionPaginationAction users = reaction.retrieveUsers().cache(false);
        CachedReaction.CachedReactionBuilder builder = CachedReaction.builder();

        List<AUser> ausers = new ArrayList<>();
        users.forEachAsync(user -> {
            ausers.add(AUser.builder().id(user.getIdLong()).build());
            return false;
        }).thenAccept(o -> future.complete(builder.build()));
        builder.users(ausers);
        builder.emote(emoteService.buildAEmoteFromReaction(reaction.getReactionEmote()));
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

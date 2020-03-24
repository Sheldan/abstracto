package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.models.embed.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component @Slf4j
@CacheConfig(cacheNames = {"messages"})
public class MessageCacheBean implements MessageCache {

    @Autowired
    private Bot bot;

    @Override
    @CachePut(key = "#message.id")
    public CachedMessage putMessageInCache(Message message) {
        log.debug("Adding message {} to cache", message.getId());
        return buildCachedMessageFromMessage(message);
    }

    @CachePut(key = "#message.messageId")
    public CachedMessage putMessageInCache(CachedMessage message) {
        return message;
    }

    @Override
    public CachedMessage getMessageFromCache(Message message) throws ExecutionException, InterruptedException {
        log.debug("Retrieving message {}", message.getId());
        return getMessageFromCache(message.getGuild().getIdLong(), message.getChannel().getIdLong(), message.getIdLong());
    }

    @Override
    @Cacheable(key = "#messageId.toString()")
    public CachedMessage getMessageFromCache(Long guildId, Long textChannelId, Long messageId) throws ExecutionException, InterruptedException {
        log.info("Retrieving message with parameters");

        CompletableFuture<CachedMessage> cachedMessageCompletableFuture =
                getMessage(guildId, textChannelId, messageId)
                        .thenApply(jdaMessage -> {
                            CachedMessage cachedMessage = buildCachedMessageFromMessage(jdaMessage);
                            putMessageInCache(cachedMessage);
                            return cachedMessage;
                        });

        return cachedMessageCompletableFuture.get();
    }

    @Override
    public CompletableFuture<Message> getMessage(Long guildId, Long textChannelId, Long messageId) {
        TextChannel textChannelById = bot.getTextChannelFromServer(guildId, textChannelId);
        return textChannelById.retrieveMessageById(messageId).submit();
    }

    private CachedMessage buildCachedMessageFromMessage(Message message) {
        List<String> attachmentUrls = new ArrayList<>();
        message.getAttachments().forEach(attachment -> {
            attachmentUrls.add(attachment.getProxyUrl());
        });
        List<CachedEmbed> embeds = new ArrayList<>();
        message.getEmbeds().forEach(embed -> {
            embeds.add(getCachedEmbedFromEmbed(embed));
        });
        return CachedMessage.builder()
                .authorId(message.getAuthor().getIdLong())
                .serverId(message.getGuild().getIdLong())
                .channelId(message.getChannel().getIdLong())
                .content(message.getContentRaw())
                .embeds(embeds)
                .timeCreated(message.getTimeCreated())
                .attachmentUrls(attachmentUrls)
                .build();
    }

    private CachedEmbed getCachedEmbedFromEmbed(MessageEmbed embed) {
        CachedEmbed.CachedEmbedBuilder builder = CachedEmbed
                .builder();
        MessageEmbed.AuthorInfo author = embed.getAuthor();
        if(author != null) {
            builder.author(EmbedAuthor.builder().avatar(author.getProxyIconUrl()).url(author.getUrl()).name(author.getName()).build());
        }
        List<MessageEmbed.Field> fields = embed.getFields();
        if(!fields.isEmpty()) {
            List<EmbedField> embedFields = new ArrayList<>();
            fields.forEach(field -> {
               EmbedField build = EmbedField
                       .builder()
                       .name(field.getName())
                       .value(field.getValue())
                       .inline(field.isInline())
                       .build();
               embedFields.add(build);
            });
            builder.fields(embedFields);
        }
        MessageEmbed.ImageInfo image = embed.getImage();
        if(image != null) {
            builder.imageUrl(image.getProxyUrl());
        }
        Color color = embed.getColor();
        if(color != null) {
            EmbedColor build = EmbedColor
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
            EmbedFooter build = EmbedFooter
                    .builder()
                    .icon(footer.getProxyIconUrl())
                    .text(footer.getText())
                    .build();
            builder.footer(build);
        }

        return builder.build();
    }
}

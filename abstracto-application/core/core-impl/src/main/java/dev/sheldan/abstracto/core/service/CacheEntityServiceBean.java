package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CacheEntityServiceBean implements CacheEntityService {

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    @Lazy
    private CacheEntityServiceBean concreteSelf;

    @Override
    public CachedEmote getCachedEmoteFromEmote(Emote emote, Guild guild) {
         return CachedEmote.builder()
                .emoteId(emote.getIdLong())
                .emoteName(emote.getName())
                .imageURL(emote.getImageUrl())
                .external(!emoteService.emoteIsFromGuild(emote, guild))
                .custom(true)
                .animated(emote.isAnimated())
                .build();
    }

    @Override
    public CachedEmote getCachedEmoteFromEmote(MessageReaction.ReactionEmote emote, Guild guild) {
        if(emote.isEmoji()) {
            return CachedEmote.builder()
                    .emoteName(emote.getName())
                    .custom(false)
                    .build();
        } else {
            return CachedEmote.builder()
                    .emoteId(emote.getIdLong())
                    .emoteName(emote.getName())
                    .imageURL(emote.getEmote().getImageUrl())
                    .external(emoteService.emoteIsFromGuild(emote.getEmote(), guild))
                    .custom(true)
                    .animated(emote.getEmote().isAnimated())
                    .build();
        }
    }

    @Override
    public CachedAttachment getCachedAttachment(Message.Attachment attachment) {
        return CachedAttachment
                .builder()
                .fileName(attachment.getFileName())
                .height(attachment.getHeight())
                .proxyUrl(attachment.getProxyUrl())
                .size(attachment.getSize())
                .url(attachment.getUrl())
                .width(attachment.getWidth())
                .build();
    }

    @Override
    public CachedEmbed getCachedEmbedFromEmbed(MessageEmbed embed) {
        CachedEmbed.CachedEmbedBuilder builder = CachedEmbed
                .builder();
        MessageEmbed.AuthorInfo author = embed.getAuthor();
        if(author != null) {
            builder.author(CachedEmbedAuthor.builder().avatar(author.getProxyIconUrl()).url(author.getUrl()).name(author.getName()).build());
        }
        List<MessageEmbed.Field> fields = embed.getFields();
        if(!fields.isEmpty()) {
            log.trace("Caching {} fields.", fields.size());
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
            builder.cachedImageInfo(buildCachedImage(image));
        }
        MessageEmbed.Thumbnail thumbnail = embed.getThumbnail();
        if(thumbnail != null) {
            builder.cachedThumbnail(buildCachedThumbnail(thumbnail));
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

    @Override
    public CachedThumbnail buildCachedThumbnail(MessageEmbed.Thumbnail thumbnail) {
        return CachedThumbnail
                .builder()
                .height(thumbnail.getHeight())
                .proxyUrl(thumbnail.getProxyUrl())
                .width(thumbnail.getWidth())
                .build();
    }

    @Override
    public CachedImageInfo buildCachedImage(MessageEmbed.ImageInfo image) {
        return CachedImageInfo
                .builder()
                .height(image.getHeight())
                .proxyUrl(image.getProxyUrl())
                .url(image.getUrl())
                .width(image.getWidth())
                .build();
    }

    @Override
    public CompletableFuture<CachedReactions> getCachedReactionFromReaction(MessageReaction reaction) {
        CompletableFuture<CachedReactions> future = new CompletableFuture<>();
        ReactionPaginationAction users = reaction.retrieveUsers().cache(false);
        CachedReactions.CachedReactionsBuilder builder = CachedReactions.builder();

        List<ServerUser> aUsers = new ArrayList<>();
        users.forEachAsync(user -> {
            log.trace("Loading user {} for reaction.", user.getIdLong());
            if(reaction.getGuild() != null) {
                aUsers.add(ServerUser.builder().userId(user.getIdLong()).serverId(reaction.getGuild().getIdLong()).build());
            }
            return false;
        }).whenComplete((o, throwable) -> {
            log.trace("{} Users have been loaded. Completing future.", aUsers.size());
            if(throwable != null) {
                log.error("Reaction user retrieval failed. Completing with what we have.", throwable);
            }
            builder.users(aUsers);
            builder.self(reaction.isSelf());
            builder.emote(getCachedEmoteFromEmote(reaction.getReactionEmote(), reaction.getGuild()));
            future.complete(builder.build());
        });
        return future;
    }

    @Override
    public CompletableFuture<CachedMessage> buildCachedMessageFromMessage(Message message) {
        CompletableFuture<CachedMessage> future = new CompletableFuture<>();
        List<CachedAttachment> attachments = new ArrayList<>();
        log.trace("Caching {} attachments.", message.getAttachments().size());
        message.getAttachments().forEach(attachment ->
                attachments.add(getCachedAttachment(attachment))
        );
        log.trace("Caching {} embeds.", message.getEmbeds().size());
        List<CachedEmbed> embeds = new ArrayList<>();
        message.getEmbeds().forEach(embed ->
                embeds.add(getCachedEmbedFromEmbed(embed))
        );

        log.trace("Caching {} emotes.", message.getEmbeds().size());
        List<CachedEmote> emotes = new ArrayList<>();
        if(message.isFromGuild()) {
            message.getEmotesBag().forEach(emote -> emotes.add(getCachedEmoteFromEmote(emote, message.getGuild())));
        }

        List<CompletableFuture<CachedReactions>> futures = new ArrayList<>();
        log.trace("Caching {} reactions.", message.getReactions().size());
        message.getReactions().forEach(messageReaction -> futures.add(getCachedReactionFromReaction(messageReaction)));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(aVoid ->
                {
                    CachedAuthor cachedAuthor = CachedAuthor.builder().authorId(message.getAuthor().getIdLong()).isBot(message.getAuthor().isBot()).build();
                    CachedMessage.CachedMessageBuilder builder = CachedMessage.builder()
                            .author(cachedAuthor)
                            .messageId(message.getIdLong())
                            .channelId(message.getChannel().getIdLong())
                            .content(message.getContentRaw())
                            .embeds(embeds)
                            .emotes(emotes)
                            .reactions(convertReactionFuturesToCachedReactions(futures))
                            .timeCreated(Instant.from(message.getTimeCreated()))
                            .attachments(attachments);
                    if(message.isFromGuild()) {
                        builder.serverId(message.getGuild().getIdLong());
                    }
                    future.complete(builder
                            .build());
                }
        ).exceptionally(throwable -> {
            log.error("Failed to load reactions for message {}. ", message.getId(), throwable);
            return null;
        });
        if(message.getReactions().isEmpty()) {
            CachedAuthor cachedAuthor = CachedAuthor.builder().authorId(message.getAuthor().getIdLong()).isBot(message.getAuthor().isBot()).build();
            CachedMessage.CachedMessageBuilder builder = CachedMessage.builder()
                    .author(cachedAuthor)
                    .messageId(message.getIdLong())
                    .channelId(message.getChannel().getIdLong())
                    .content(message.getContentRaw())
                    .embeds(embeds)
                    .emotes(emotes)
                    .timeCreated(Instant.from(message.getTimeCreated()))
                    .attachments(attachments);
            if(message.isFromGuild()) {
                builder.serverId(message.getGuild().getIdLong());
            }
            future.complete(builder
                    .build());
        }
        return future;
    }

    private List<CachedReactions> convertReactionFuturesToCachedReactions(List<CompletableFuture<CachedReactions>> futures) {
        return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

}

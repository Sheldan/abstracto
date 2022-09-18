package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.cache.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CacheEntityService {
    CachedEmote getCachedEmoteFromEmote(CustomEmoji emote, Guild guild);
    CachedEmote getCachedEmoteFromEmote(Emoji emote, Guild guild);
    CachedAttachment getCachedAttachment(Message.Attachment attachment);
    CachedEmbed getCachedEmbedFromEmbed(MessageEmbed embed);
    List<CachedAttachment> getCachedAttachments(List<Message.Attachment> attachments);
    CachedThumbnail buildCachedThumbnail(MessageEmbed.Thumbnail thumbnail);
    CachedImageInfo buildCachedImage(MessageEmbed.ImageInfo image);
    CompletableFuture<CachedReactions> getCachedReactionFromReaction(MessageReaction reaction);
    CompletableFuture<CachedMessage> buildCachedMessageFromMessage(Message message, boolean loadReferenced);
    CompletableFuture<CachedMessage> buildCachedMessageFromMessage(Message message);
}

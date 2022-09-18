package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.database.AEmote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface EmoteService {
    boolean isEmoteUsableByBot(CustomEmoji emote);
    AEmote buildAEmoteFromReaction(Emoji reaction);
    String getEmoteAsMention(AEmote emote, Long serverId, String defaultText);
    String getEmoteAsMention(AEmote emote, Long serverId);
    String getUsableEmoteOrDefault(Long serverId, String name);
    void throwIfEmoteDoesNotExist(String emoteKey, Long serverId);
    AEmote getEmoteOrDefaultEmote(String emoteKey, Long serverId);
    String getDefaultEmote(String emoteKey);
    boolean isReactionEmoteAEmote(Emoji reaction, AEmote storedEmote);
    Optional<CachedReactions> getReactionFromMessageByEmote(CachedMessage message, AEmote emote);
    boolean compareAEmote(AEmote a, AEmote b);
    boolean compareCachedEmoteWithAEmote(CachedEmote a, AEmote b);
    AEmote getFakeEmote(Object object);
    AEmote getFakeEmoteFromEmote(CustomEmoji emote);
    AEmote getFakeEmoteFromEmoji(Emoji emoji);
    boolean emoteIsFromGuild(CustomEmoji emote, Guild guild);
    CompletableFuture<CustomEmoji> getEmoteFromCachedEmote(CachedEmote cachedEmote);

    Optional<CustomEmoji> getEmote(Long serverId, AEmote emote);
    Optional<Emoji> getEmote(AEmote emote);
}

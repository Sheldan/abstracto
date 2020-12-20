package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.database.AEmote;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface EmoteService {
    boolean isEmoteUsableByBot(Emote emote);
    AEmote buildAEmoteFromReaction(MessageReaction.ReactionEmote reaction);
    String getEmoteAsMention(AEmote emote, Long serverId, String defaultText);
    String getEmoteAsMention(AEmote emote, Long serverId);
    String getUsableEmoteOrDefault(Long serverId, String name);
    void throwIfEmoteDoesNotExist(String emoteKey, Long serverId);
    AEmote getEmoteOrDefaultEmote(String emoteKey, Long serverId);
    String getDefaultEmote(String emoteKey);
    boolean isReactionEmoteAEmote(MessageReaction.ReactionEmote reaction, AEmote storedEmote);
    Optional<CachedReactions> getReactionFromMessageByEmote(CachedMessage message, AEmote emote);
    boolean compareAEmote(AEmote a, AEmote b);
    boolean compareCachedEmoteWithAEmote(CachedEmote a, AEmote b);
    AEmote getFakeEmote(Object object);
    AEmote getFakeEmoteFromEmote(Emote emote);
    boolean emoteIsFromGuild(Emote emote, Guild guild);
    CompletableFuture<Emote> getEmoteFromCachedEmote(CachedEmote cachedEmote);
}

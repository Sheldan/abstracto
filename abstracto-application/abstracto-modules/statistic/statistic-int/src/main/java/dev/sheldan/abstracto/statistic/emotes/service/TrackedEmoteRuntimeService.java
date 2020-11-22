package dev.sheldan.abstracto.statistic.emotes.service;

import dev.sheldan.abstracto.statistic.emotes.model.PersistingEmote;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;
import java.util.Map;

public interface TrackedEmoteRuntimeService {
    Map<Long, Map<Long, List<PersistingEmote>>> getRuntimeConfig();
    void addEmoteForServer(Emote emote, Guild guild, boolean external);
    void addEmoteForServer(Emote emote, Guild guild, Long count, boolean external);
    Long getKey();
    PersistingEmote createFromEmote(Guild guild, Emote emote, boolean external);
    PersistingEmote createFromEmote(Guild guild, Emote emote, Long count, boolean external);
    void takeLock();
    void releaseLock();
}

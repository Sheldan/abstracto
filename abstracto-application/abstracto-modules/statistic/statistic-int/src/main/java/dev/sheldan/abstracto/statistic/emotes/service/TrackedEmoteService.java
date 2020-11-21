package dev.sheldan.abstracto.statistic.emotes.service;

import dev.sheldan.abstracto.statistic.emotes.model.PersistingEmote;
import dev.sheldan.abstracto.statistic.emotes.model.TrackedEmoteOverview;
import dev.sheldan.abstracto.statistic.emotes.model.TrackedEmoteSynchronizationResult;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Map;
import java.util.List;

public interface TrackedEmoteService {
    void addEmoteToRuntimeStorage(List<Emote> emotes, Guild guild);
    void addEmoteToRuntimeStorage(Emote emote, Guild guild, Long count);
    void storeEmoteStatistics(Map<Long, List<PersistingEmote>> usagesToStore);
    TrackedEmote getFakeTrackedEmote(Emote emote, Guild guild);
    TrackedEmote getFakeTrackedEmote(Long id, Guild guild);
    TrackedEmoteSynchronizationResult synchronizeTrackedEmotes(Guild guild);
    TrackedEmoteOverview loadTrackedEmoteOverview(Guild guild);
    TrackedEmoteOverview loadTrackedEmoteOverview(Guild guild, Boolean showTrackingDisabled);
    TrackedEmote createFakeTrackedEmote(Emote emote, Guild guild);
    TrackedEmote createFakeTrackedEmote(Emote emote, Guild guild, boolean external);
    void deleteTrackedEmote(TrackedEmote trackedEmote);
    void resetEmoteStats(Guild guild);
    void disableEmoteTracking(Guild guild);
}

package dev.sheldan.abstracto.statistic.emotes.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsModel;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;

import java.time.Instant;

public interface UsedEmoteService {
    EmoteStatsModel getEmoteStatsForServerSince(AServer server, Instant since);
    EmoteStatsModel getDeletedEmoteStatsForServerSince(AServer server, Instant since);
    EmoteStatsModel getExternalEmoteStatsForServerSince(AServer server, Instant since);
    EmoteStatsModel getActiveEmoteStatsForServerSince(AServer server, Instant since);
    void purgeEmoteUsagesSince(TrackedEmote emote, Instant since);
    void purgeEmoteUsages(TrackedEmote emote);
}

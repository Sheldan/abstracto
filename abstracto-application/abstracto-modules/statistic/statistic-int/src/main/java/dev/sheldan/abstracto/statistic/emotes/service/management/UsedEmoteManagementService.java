package dev.sheldan.abstracto.statistic.emotes.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsResult;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UsedEmoteManagementService {
    Optional<UsedEmote> loadUsedEmoteForTrackedEmoteToday(TrackedEmote trackedEmote);
    UsedEmote createEmoteUsageForToday(TrackedEmote trackedEmote, Long count);
    List<UsedEmote> loadEmoteUsagesForServerSince(AServer server, Instant since);
    List<EmoteStatsResult> loadAllEmoteStatsForServerSince(AServer server, Instant since);
    List<EmoteStatsResult> loadDeletedEmoteStatsForServerSince(AServer server, Instant since);
    List<EmoteStatsResult> loadExternalEmoteStatsForServerSince(AServer server, Instant since);
    List<EmoteStatsResult> loadActiveEmoteStatsForServerSince(AServer server, Instant since);
    void purgeEmoteUsagesSince(TrackedEmote emote, Instant since);
}

package dev.sheldan.abstracto.statistic.emotes.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsResult;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for creating/updating/deleting {@link UsedEmote} instances in the database
 */
public interface UsedEmoteManagementService {
    /**
     * Loads an {@link Optional} containing a {@link UsedEmote} for the particular {@link TrackedEmote} for the current day.
     * The {@link Optional} is empty, if none exists.
     * @param trackedEmote The {@link TrackedEmote} to search  a {@link UsedEmote} for today
     * @return An {@link Optional} containing a {@link UsedEmote}, if it exists for the current day
     */
    Optional<UsedEmote> loadUsedEmoteForTrackedEmoteToday(TrackedEmote trackedEmote);

    /**
     * Creates and persists and instance of {@link UsedEmote} from the given {@link TrackedEmote}, with the defined count and the current date.
     * @param trackedEmote The {@link TrackedEmote} for which to create a {@link UsedEmote} for
     * @param count The amount of usages for the {@link UsedEmote}
     * @return The created {@link UsedEmote} instance int he database
     */
    UsedEmote createEmoteUsageForToday(TrackedEmote trackedEmote, Long count);

    /**
     * Loads {@link UsedEmote} for the {@link AServer} which are newer than the given {@link Instant}
     * @param server The {@link AServer} to retrieve the {@link UsedEmote} for
     * @param since The {@link Instant} since when the emote stats should be retrieved. Only the date portion is considered.
     * @return A list of {@link UsedEmote} from the {@link AServer} newer than the given {@link Instant}
     */
    List<UsedEmote> loadEmoteUsagesForServerSince(AServer server, Instant since);

    /**
     * Load {@link EmoteStatsResult} from the {@link AServer} for all {@link TrackedEmote} which are newer than {@link Instant}
     * @param server The {@link AServer} to retrieve the emote statistics for
     * @param since Emote stats should be younger than this {@link Instant}. Only the date portion is considered.
     * @return A list of {@link EmoteStatsResult} from the {@link AServer} newer than the given {@link Instant} for all {@link TrackedEmote}
     */
    List<EmoteStatsResult> loadAllEmoteStatsForServerSince(AServer server, Instant since);

    /**
     * Load {@link EmoteStatsResult} from the {@link AServer} for {@link TrackedEmote} which were deleted and newer than {@link Instant}
     * @param server The {@link AServer} to retrieve the emote statistics for
     * @param since Emote stats should be younger than this {@link Instant}. Only the date portion is considered.
     * @return A list of {@link EmoteStatsResult} from the {@link AServer} newer than the given {@link Instant} for all deleted {@link TrackedEmote}
     */
    List<EmoteStatsResult> loadDeletedEmoteStatsForServerSince(AServer server, Instant since);

    /**
     * Load {@link EmoteStatsResult} from the {@link AServer} for {@link TrackedEmote} which are external and newer than {@link Instant}
     * @param server The {@link AServer} to retrieve the emote statistic for
     * @param since Emote stats should be younger than this {@link Instant}. Only the date portion is considered.
     * @return A list of {@link EmoteStatsResult} from the {@link AServer} newer than the given {@link Instant} for all external {@link TrackedEmote}
     */
    List<EmoteStatsResult> loadExternalEmoteStatsForServerSince(AServer server, Instant since);

    /**
     * Load {@link EmoteStatsResult} from the {@link AServer} for {@link TrackedEmote} which are active and newer than {@link Instant}
     * @param server The {@link AServer} to retrieve the emote statistic for
     * @param since Emote stats should be younger than this {@link Instant}. Only the date portion is considered.
     * @return A list of {@link EmoteStatsResult} from the {@link AServer} newer than the given {@link Instant} for all active {@link TrackedEmote}
     */
    List<EmoteStatsResult> loadActiveEmoteStatsForServerSince(AServer server, Instant since);

    /**
     * Deletes all emote usages for the {@link TrackedEmote} which are younger than the given {@link Instant}
     * @param emote The {@link TrackedEmote} to remove the usages for
     * @param since All emote stats which are newer than this {@link Instant} will be deleted
     */
    void purgeEmoteUsagesSince(TrackedEmote emote, Instant since);
}

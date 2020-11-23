package dev.sheldan.abstracto.statistic.emotes.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsModel;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;

import java.time.Instant;

/**
 * Service responsible to provide operations on {@link dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote}
 */
public interface UsedEmoteService {
    /**
     * Retrieves the {@link EmoteStatsModel} for the {@link AServer} since {@link Instant}.
     * This {@link EmoteStatsModel} will contain all {@link TrackedEmote} from the server
     * @param server The {@link AServer} to retrieve the emote stats for
     * @param since Emote stats should be younger than this {@link Instant}. Only the date portion is considered.
     * @return An {@link EmoteStatsModel} containing the statistics split by animated and static {@link net.dv8tion.jda.api.entities.Emote}
     */
    EmoteStatsModel getEmoteStatsForServerSince(AServer server, Instant since);

    /**
     * Retrieves the {@link EmoteStatsModel} for the {@link AServer} since {@link Instant}.
     * This {@link EmoteStatsModel} will contain only deleted {@link TrackedEmote} from the server
     * @param server The {@link AServer} to retrieve the emote stats for
     * @param since Emote stats should be younger than this {@link Instant}. Only the date portion is considered.
     * @return An {@link EmoteStatsModel} containing the statistics split by animated and static {@link net.dv8tion.jda.api.entities.Emote}
     */
    EmoteStatsModel getDeletedEmoteStatsForServerSince(AServer server, Instant since);

    /**
     * Retrieves the {@link EmoteStatsModel} for the {@link AServer} since {@link Instant}.
     * This {@link EmoteStatsModel} will contain only external {@link TrackedEmote} from the server
     * @param server The {@link AServer} to retrieve the emote stats for
     * @param since Emote stats should be younger than this {@link Instant}. Only the date portion is considered.
     * @return An {@link EmoteStatsModel} containing the statistics split by animated and static {@link net.dv8tion.jda.api.entities.Emote}
     */
    EmoteStatsModel getExternalEmoteStatsForServerSince(AServer server, Instant since);

    /**
     * Retrieves the {@link EmoteStatsModel} for the {@link AServer} since {@link Instant}.
     * This {@link EmoteStatsModel} will contain only active {@link TrackedEmote} from the server. These are emotes which are still present
     * the {@link net.dv8tion.jda.api.entities.Guild}
     * @param server The {@link AServer} to retrieve the emote stats for
     * @param since Emote stats should be younger than this {@link Instant}. Only the date portion is considered.
     * @return An {@link EmoteStatsModel} containing the statistics split by animated and static {@link net.dv8tion.jda.api.entities.Emote}
     */
    EmoteStatsModel getActiveEmoteStatsForServerSince(AServer server, Instant since);

    /**
     * Removes all {@link dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote} for the given {@link TrackedEmote} which are younger
     * than the given {@link Instant}
     * @param emote The {@link TrackedEmote} which should have its usages removed
     * @param since {@link dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote} younger than this {@link Instant} shold be remoed. Only the date porition is considered.
     */
    void purgeEmoteUsagesSince(TrackedEmote emote, Instant since);

    /**
     * Removes *all* {@link dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote} for the given {@link TrackedEmote}.
     * @param emote The {@link TrackedEmote} which should have its usages removed
     */
    void purgeEmoteUsages(TrackedEmote emote);
}

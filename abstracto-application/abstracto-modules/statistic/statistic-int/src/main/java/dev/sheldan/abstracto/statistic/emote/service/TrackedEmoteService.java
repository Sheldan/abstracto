package dev.sheldan.abstracto.statistic.emote.service;

import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.statistic.emote.model.PersistingEmote;
import dev.sheldan.abstracto.statistic.emote.model.TrackedEmoteOverview;
import dev.sheldan.abstracto.statistic.emote.model.TrackedEmoteSynchronizationResult;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.model.database.UsedEmoteType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;

import java.util.List;
import java.util.Map;

/**
 * Service responsible to provide operations on {@link TrackedEmote}
 */
public interface TrackedEmoteService {

    /**
     * Adds the given list of {@link CustomEmoji}s to the runtime storage for the given {@link Guild}
     * @param emotes The list of {@link CustomEmoji}s to add to the runtime storage
     * @param guild The {@link Guild} in which the {@link CustomEmoji}s were used and where the usages should be added
     */
    void addEmoteToRuntimeStorage(List<CachedEmote> emotes, Guild guild, UsedEmoteType usedEmoteType);

    /**
     * Adds the given {@link CustomEmoji} with the given amount to the runtime storage for the given {@link Guild}
     * @param emote The {@link CachedEmote} to add to the runtime storage
     * @param guild The {@link Guild} in which the {@link CustomEmoji} was used and in which the usage should be added
     * @param count The amount of times which the {@link CustomEmoji} has been used and should be reflected in the runtime storage
     * @param type The type of interaction the emote came from
     */
    void addEmoteToRuntimeStorage(CachedEmote emote, Guild guild, Long count, UsedEmoteType type);

    /**
     * Adds the given {@link CustomEmoji} with the given amount to the runtime storage for the given {@link Guild}
     * @param emote The {@link CustomEmoji} to add to the runtime storage
     * @param guild The {@link Guild} in which the {@link CustomEmoji} was used and in which the usage should be added
     * @param count The amount of times which the {@link CustomEmoji} has been used and should be reflected in the runtime storage
     * @param type The type of interaction the emote came from
     */
    void addEmoteToRuntimeStorage(CustomEmoji emote, Guild guild, Long count, UsedEmoteType type);

    /**
     * Takes the given map of server_ids with the list of {@link PersistingEmote} and stores the objects in the database
     * Non existing {@link TrackedEmote} for the server will be created. Depending on the feature mode external emotes will be created.
     * @param usagesToStore The map of server_ids to a List of {@link PersistingEmote} which should be stored in the database
     */
    void storeEmoteStatistics(Map<Long, List<PersistingEmote>> usagesToStore);

    /**
     * Creates a fake {@link TrackedEmote} from the given {@link CustomEmoji} and {@link Guild}. This {@link TrackedEmote}
     * is not persisted and has the fake value set to true
     * @param emote The {@link CustomEmoji} to be used for the fake {@link TrackedEmote}
     * @param guild The {@link Guild} to be used for the fake {@link TrackedEmote}
     * @return The fake {@link TrackedEmote} which was created
     */
    TrackedEmote getFakeTrackedEmote(CustomEmoji emote, Guild guild);

    /**
     * Creates a fake {@link TrackedEmote} from the given emote ID and server ID. This {@link TrackedEmote}
     * is not persisted and has the fake value set to true
     * @param emoteId The ID of an {@link CustomEmoji}
     * @param guild The ID of an {@link dev.sheldan.abstracto.core.models.database.AServer}
     * @return The fake {@link TrackedEmote} which was created
     */
    TrackedEmote getFakeTrackedEmote(Long emoteId, Guild guild);

    /**
     * Checks the currently existing {@link CustomEmoji}s in the {@link Guild} with the currently {@link TrackedEmote} and synchronizes
     * the state. This means: unknown {@link CustomEmoji} are created as {@link TrackedEmote} and already existing {@link TrackedEmote}
     * which are not found in the {@link Guild} are marked as deleted.
     * @param guild The {@link Guild} to synchronize the {@link CustomEmoji} for
     * @return The {@link TrackedEmoteSynchronizationResult} which contains information about what changed (number of deletions and additions)
     */
    TrackedEmoteSynchronizationResult synchronizeTrackedEmotes(Guild guild);

    /**
     * Loads all the {@link TrackedEmote} for the given {@link Guild} for which tracking is enabled into a {@link TrackedEmoteOverview}
     * @param guild The {@link Guild} to retrieve the {@link TrackedEmote} for
     * @return The {@link TrackedEmoteOverview} containing the {@link TrackedEmote} which have tracking enabled
     */
    TrackedEmoteOverview loadTrackedEmoteOverview(Guild guild);

    /**
     * Loads all the {@link TrackedEmote} for the given {@link Guild}. If showTrackingDisabled is true, it will also
     * show {@link TrackedEmote} for which tracking has been disabled
     * @param guild The {@link Guild} to retrieve the {@link TrackedEmoteOverview} for
     * @param showTrackingDisabled Whether or not to include {@link TrackedEmote} for which tracking has been disabled
     * @return The {@link TrackedEmoteOverview} containing the retrieved {@link TrackedEmote} depending on the criteria
     */
    TrackedEmoteOverview loadTrackedEmoteOverview(Guild guild, Boolean showTrackingDisabled);

    /**
     * Creates a {@link TrackedEmote} from the {@link CustomEmoji} based on a usage in {@link Guild}
     * This method detects if the {@link CustomEmoji} is external or not on its own.
     * @param emote The {@link CustomEmoji} to create a {@link TrackedEmote} for
     * @param guild The {@link Guild} for which the {@link TrackedEmote} should be created for
     * @return The created {@link TrackedEmote} instance in the database
     */
    TrackedEmote createTrackedEmote(CustomEmoji emote, Guild guild);

    /**
     * Creates a {@link TrackedEmote} from the {@link CustomEmoji} based on a usage in {@link Guild}
     * @param emote The {@link CustomEmoji} to create a {@link TrackedEmote} for
     * @param guild The {@link Guild} in which the {@link TrackedEmote} should be created for
     * @param external Whether or not the {@link CustomEmoji} is part of the {@link Guild} or not
     * @return The created {@link TrackedEmote} instance in the database
     */
    TrackedEmote createTrackedEmote(CustomEmoji emote, Guild guild, boolean external);

    /**
     * Deletes the referenced {@link TrackedEmote} in the database
     * @param trackedEmote The {@link TrackedEmote} to delete
     */
    void deleteTrackedEmote(TrackedEmote trackedEmote);

    /**
     * Completely resets the emote statistics in {@link Guild}. This will purge all emote usages and delete all {@link TrackedEmote}
     * of this {@link Guild}
     * @param guild The {@link Guild} to reset the emote statistics for
     */
    void resetEmoteStats(Guild guild);

    /**
     * Disables emote tracking for *every* {@link TrackedEmote} within the {@link Guild} individually. Effectively disabling
     * emote tracking within the server.
     * @param guild The {@link Guild} to disable emote tracking for
     */
    void disableEmoteTracking(Guild guild);
}

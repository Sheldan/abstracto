package dev.sheldan.abstracto.statistic.emote.service.management;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.statistic.emote.exception.TrackedEmoteNotFoundException;
import dev.sheldan.abstracto.statistic.emote.model.PersistingEmote;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

import java.util.List;
import java.util.Optional;

/**
 * Service responsible for managing {@link TrackedEmote} instances in the database
 */
public interface TrackedEmoteManagementService {
    /**
     * Creates and persists a {@link TrackedEmote} for which tracking is enabled with the given individual parameters
     * @param emoteId The ID of the {@link CustomEmoji} to track
     * @param emoteName The name of the {@link CustomEmoji} to track
     * @param animated Whether or not the {@link CustomEmoji} to track is animated
     * @param server The {@link AServer} for which the {@link CustomEmoji} should be tracked
     * @return The created {@link TrackedEmote} instance in the database
     */
    TrackedEmote createTrackedEmote(Long emoteId, String emoteName, Boolean animated, AServer server);

    /**
     * Creates and persists a {@link TrackedEmote} for which tracking is enabled based on the given {@link CustomEmoji} and {@link Guild}
     * @param emote The {@link CustomEmoji} to be used to create a {@link TrackedEmote}
     * @param guild The {@link Guild} for which the emote should be tracked for
     * @return The created {@link TrackedEmote} instance in the database
     */
    TrackedEmote createTrackedEmote(CustomEmoji emote, Guild guild);

    /**
     * Creates and persists a {@link TrackedEmote} for which tracking is enabled based on the given {@link CustomEmoji}.
     * The emote used here must contain a {@link Guild guild} instance, emotes created from {@link net.dv8tion.jda.api.entities.Message messages}
     * do not.
     * @param emote The {@link CustomEmoji} to be used to create a {@link TrackedEmote}
     * @return The created {@link TrackedEmote} instance in the database
     */
    TrackedEmote createTrackedEmote(RichCustomEmoji emote);

    /**
     * Creates and persists a {@link TrackedEmote} for which tracking is enabled based on the given {@link CustomEmoji} and {@link Guild}
     * @param emote The {@link CachedEmote} to be used to create a {@link TrackedEmote}
     * @return The created {@link TrackedEmote} instance in the database
     */
    TrackedEmote createTrackedEmote(CachedEmote emote);

    /**
     * Creates and persist a {@link TrackedEmote} for which tracking is enabled based on the given {@link CustomEmoji} and {@link Guild}
     * @param emote The {@link CustomEmoji} to be used to create a {@link TrackedEmote}
     * @param guild The {@link Guild} for which the emote should be tracked for
     * @param external Whether or not the emote is external
     * @return The created {@link TrackedEmote} instance in the database
     */
    TrackedEmote createTrackedEmote(CustomEmoji emote, Guild guild, boolean external);

    /**
     * Creates and persis a {@link TrackedEmote} based ont he given parameters
     * @param emoteId The ID of an {@link CustomEmoji}
     * @param emoteName The name of an {@link CustomEmoji}
     * @param animated Whether or not the {@link CustomEmoji} is enabled
     * @param tracked Whether or not the {@link TrackedEmote} should have tracking enabled
     * @param server The {@link AServer} for which the {@link TrackedEmote} should be created
     * @return The created {@link TrackedEmote} instance in the database
     */
    TrackedEmote createTrackedEmote(Long emoteId, String emoteName, Boolean animated, Boolean tracked, AServer server);

    /**
     * Creates an {@link TrackedEmote} based on the parameters which is external
     * @param emoteId The ID of an {@link CustomEmoji}
     * @param emoteName the name of an {@link CustomEmoji}
     * @param externalUrl The URL of the {@link CustomEmoji} which should be stored
     * @param animated Whether or not the {@link CustomEmoji} is external
     * @param server The {@link AServer} for which the {@link TrackedEmote} should be created
     * @param trackingEnabled Whether or not the {@link TrackedEmote} should have tracking enabled
     * @return The created {@link TrackedEmote} instance in the database
     */
    TrackedEmote createExternalEmote(Long emoteId, String emoteName, String externalUrl, Boolean animated, AServer server, boolean trackingEnabled);

    /**
     * Creates an {@link TrackedEmote} based on the parameters which is not being tracked
     * @param emoteId The ID of an {@link CustomEmoji}
     * @param emoteName The name of an {@link CustomEmoji}
     * @param animated Whether or not the {@link CustomEmoji} is animated
     * @param server The {@link AServer} for which the {@link TrackedEmote} should be created
     * @return The created {@link TrackedEmote} instance in the database
     */
    TrackedEmote createNotTrackedEmote(Long emoteId, String emoteName, Boolean animated, AServer server);

    /**
     * Creates an {@link TrackedEmote} based on the {@link PersistingEmote} which is being tracked
     * @param persistingEmote The {@link PersistingEmote} to create the {@link TrackedEmote} based of
     * @return The created {@link TrackedEmote} instance in the database
     */
    TrackedEmote createExternalTrackedEmote(PersistingEmote persistingEmote);

    /**
     * Creates an external {@link TrackedEmote} based on the {@link CustomEmoji} and the {@link Guild}
     * @param emote The {@link CustomEmoji} to be used to create an external {@link TrackedEmote}
     * @param guild The {@link Guild} for which the emote should be tracked for
     * @return The create {@link TrackedEmote} instance in the database
     */
    TrackedEmote createExternalTrackedEmote(CustomEmoji emote, Guild guild);

    /**
     * Marks the {@link TrackedEmote} identified by serverId and emoteId as deleted
     * @param serverId The ID of the server to mark the {@link TrackedEmote} as deleted
     * @param emoteId The ID of the {@link CustomEmoji} to mark the {@link TrackedEmote} as deleted
     * @throws TrackedEmoteNotFoundException if no {@link TrackedEmote} with the given IDs can be found
     */
    void markAsDeleted(Long serverId, Long emoteId);

    /**
     * Marks the {@link CustomEmoji emote} as deleted in the database. This {@link CustomEmoji emote} must
     * not come from a {@link net.dv8tion.jda.api.entities.Message message}, because then the {@link Guild guild}
     * is null.
     * @throws TrackedEmoteNotFoundException if no {@link TrackedEmote} with the given IDs can be found
     */
    void markAsDeleted(RichCustomEmoji emote);

    /**
     * Marks the given {@link TrackedEmote} as deleted
     * @param trackedEmote The {@link TrackedEmote} which should be marked as deleted
     */
    void markAsDeleted(TrackedEmote trackedEmote);

    /**
     * Retrieves a {@link TrackedEmote} by the given emoteID and serverID
     * @param emoteId The ID of the {@link AServer} so search for
     * @param serverId The ID Of the {@link CustomEmoji} to search for
     * @return The found {@link TrackedEmote} instance if, one exists
     * @throws TrackedEmoteNotFoundException if no {@link TrackedEmote} with the given IDs can be found
     */
    TrackedEmote loadByEmoteId(Long emoteId, Long serverId);

    /**
     * Loads a {@link TrackedEmote} by the given {@link CustomEmoji}. The ID necessary for the server is the {@link Guild} from the emote.
     * The {@link CustomEmoji} must containing a {@link Guild} object, this is not guaranteed, but this implementation relies on it.
     * @param emote The {@link CustomEmoji} to find a {@link TrackedEmote} for
     * @return The {@link TrackedEmote} which was found
     * @throws TrackedEmoteNotFoundException if no {@link TrackedEmote} with the given IDs can be found
     */
    TrackedEmote loadByEmote(RichCustomEmoji emote);

    /**
     * Checks whether or not a {@link TrackedEmote} with the given emote ID and server ID exists
     * @param emoteId The ID of an {@link CustomEmoji} to check
     * @param serverId the ID of an {@link AServer} to check
     * @return Whether or not a {@link TrackedEmote} with the given IDs exists
     */
    boolean trackedEmoteExists(Long emoteId, Long serverId);

    /**
     * Loads a {@link TrackedEmote} based on its composite keys represented by a {@link ServerSpecificId}
     * @param trackedEmoteServer The {@link ServerSpecificId} to retrieve a {@link TrackedEmote} from
     * @return The found {@link TrackedEmote} based on the given parameters
     * @throws TrackedEmoteNotFoundException if no {@link TrackedEmote} with the given {@link ServerSpecificId} was found
     */
    TrackedEmote loadByTrackedEmoteServer(ServerSpecificId trackedEmoteServer);

    /**
     * Searches for a {@link TrackedEmote} by the given emoteId and serverId and returns an {@link Optional} containing the value, if any.
     * @param emoteId The ID of the {@link CustomEmoji} to search for
     * @param serverId The ID of the {@link AServer} to search for
     * @return An {@link Optional} containing a {@link TrackedEmote} if it exists, empty otherwise
     */
    Optional<TrackedEmote> loadByEmoteIdOptional(Long emoteId, Long serverId);

    /**
     * Retrieves all {@link TrackedEmote} for a {@link AServer}, which are not yet deleted and not external
     * @param server The {@link AServer} to retrieve the active {@link TrackedEmote} for
     * @return A list of {@link TrackedEmote} which are currently considered active from the {@link AServer}
     */
    List<TrackedEmote> getAllActiveTrackedEmoteForServer(AServer server);

    /**
     * Retrieves all {@link TrackedEmote} for an ID of a {@link AServer}, which are not yet deleted and not external
     * @param serverId The ID of an {@link AServer} ro retrieve the active {@link TrackedEmote} for
     * @return A list of {@link TrackedEmote} which are currently considered active from the {@link AServer} with the ID
     */
    List<TrackedEmote> getAllActiveTrackedEmoteForServer(Long serverId);

    /**
     * Retrieves *all* tracking enabled {@link TrackedEmote} for an ID of a {@link AServer}, with the option to also retrieve the ones for which tracking is disable
     * @param serverId The ID of an {@link AServer} to retrieve the {@link TrackedEmote} for
     * @param showTrackingDisabledEmotes Whether or not tracking disabled {@link TrackedEmote} should be retrieved as well
     * @return A list of {@link TrackedEmote} from an {@link AServer} with the given ID
     */
    List<TrackedEmote> getTrackedEmoteForServer(Long serverId, Boolean showTrackingDisabledEmotes);

    /**
     * Changes the name of the given {@link TrackedEmote} to a new value
     * @param emote The {@link TrackedEmote} to change the name of
     * @param name The new name to change to
     */
    void changeName(TrackedEmote emote, String name);

    /**
     * Disables the tracking of a {@link TrackedEmote}
     * @param emote The {@link TrackedEmote} to disable the tracking of
     */
    void disableTrackedEmote(TrackedEmote emote);

    /**
     * Enables the tracking of a {@link TrackedEmote}
     * @param emote The {@link TrackedEmote} to enable the tracking of
     */
    void enableTrackedEmote(TrackedEmote emote);

    /**
     * Deletes the given {@link TrackedEmote}
     * @param emote The {@link TrackedEmote} to delete
     */
    void deleteTrackedEmote(TrackedEmote emote);
}

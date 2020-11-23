package dev.sheldan.abstracto.statistic.emotes.service;

import dev.sheldan.abstracto.statistic.emotes.model.PersistingEmote;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;
import java.util.Map;

/**
 * Service responsible for managing and containing the runtime storage for emote statistics
 */
public interface TrackedEmoteRuntimeService {

    /**
     * Returns the current runtime configuration. You should acquire the lock with `takeLock` before.
     * @return The Map containing the current runtime emote stats
     */
    Map<Long, Map<Long, List<PersistingEmote>>> getRuntimeConfig();

    /**
     * Adds the given {@link Emote} used in the {@link Guild} to the runtime storage.
     * The necessary lock will be acquired by this method.
     * @param emote The {@link Emote} to add to the runtime storage
     * @param guild The {@link Guild} in which the {@link Emote} is used
     * @param external Whether or not the emote is external
     */
    void addEmoteForServer(Emote emote, Guild guild, boolean external);

    /**
     * Adds the given {@link Emote} used in the {@link Guild} to the runtime storage.
     * The necessary lock will be acquired by this method.
     * @param emote The {@link Emote} to add to the runtime storage
     * @param guild The {@link Guild} in which the {@link Emote} is used
     * @param count The amount of usages which should be added
     * @param external Whether or not the emote is external
     */
    void addEmoteForServer(Emote emote, Guild guild, Long count, boolean external);

    /**
     * Calculates the key used for the Map containing the emote statistics.
     * @return The calculated key to be used in the Map
     */
    Long getKey();

    /**
     * Creates a {@link PersistingEmote} from the given parameters.
     * @param guild The {@link Guild} in which the {@link Emote} is used
     * @param emote The {@link Emote} to create a {@link PersistingEmote} from
     * @param external Whether or not the {@link Emote} is external
     * @return A created {@link PersistingEmote} instance from the {@link Emote}
     */
    PersistingEmote createFromEmote(Guild guild, Emote emote, boolean external);

    /**
     * Creates a {@link PersistingEmote} from the given parameters.
     * @param guild The {@link Guild} in which the {@link Emote} is used
     * @param emote The {@link Emote} to create a {@link PersistingEmote} from
     * @param count The amount of usages the {@link Emote} has been used
     * @param external Whether or not the {@link Emote} is external
     * @return A created {@link PersistingEmote} instance from the {@link Emote}
     */
    PersistingEmote createFromEmote(Guild guild, Emote emote, Long count, boolean external);

    /**
     * Acquires the lock which should be used when accessing the runtime storage
     */
    void takeLock();

    /**
     * Releases the lock which should be used then accessing the runtime storage
     */
    void releaseLock();
}

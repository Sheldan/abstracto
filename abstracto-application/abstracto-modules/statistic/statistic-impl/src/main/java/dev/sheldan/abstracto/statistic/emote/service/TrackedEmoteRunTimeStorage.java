package dev.sheldan.abstracto.statistic.emote.service;

import dev.sheldan.abstracto.statistic.emote.model.PersistingEmote;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component actually containing the data structure containing the runtime storage for emote statistics.
 */
@Component
public class TrackedEmoteRunTimeStorage {
    /**
     * A map of *minutes* containing a map of server_ids containing a list of {@link PersistingEmote}, which contains
     * all necessary information about the emotes which were used.
     * The top most map contains all the different minutes in which there were used emotes. Mostly this
     * map will not contain many keys, because the {@link dev.sheldan.abstracto.statistic.emote.job.EmotePersistingJob}
     * will remove them a minute later. The Map within the current minute will contain every server as a key in which
     * there were emotes used in the particular minute. {@link PersistingEmote} does not contain any JDA related objects
     * but only the information necessary to identify any {@link net.dv8tion.jda.api.entities.Emote}.
     */
    private HashMap<Long, Map<Long, List<PersistingEmote>>> trackedEmotes = new HashMap<>();

    public Map<Long, Map<Long, List<PersistingEmote>>> getRuntimeConfig() {
        return trackedEmotes;
    }

    /**
     * Whether or not the minute has already been tracked.
     * @param key The minute since 1970 to check for
     * @return Whether or not the minute already has an entry
     */
    public boolean contains(Long key) {
        return trackedEmotes.containsKey(key);
    }

    /**
     * Adds the minute identified by the {@link Long} since 1970 into the Map, with the associated Map of server_ids and
     * {@link PersistingEmote}.
     * @param key The minute since 1970 to add
     * @param objectToPut The Map of server_ids mapping to List of {@link PersistingEmote} toa dd
     */
    public void put(Long key, Map<Long, List<PersistingEmote>> objectToPut) {
        trackedEmotes.put(key, objectToPut);
    }

    /**
     * Retrieves an entry identified by the minute since 1970
     * @param key The key of the minute to retrieve
     * @return The Map of server_ids and {@link PersistingEmote} which already exists for the given minute
     */
    public Map<Long, List<PersistingEmote>> get(Long key) {
        return trackedEmotes.get(key);
    }
}

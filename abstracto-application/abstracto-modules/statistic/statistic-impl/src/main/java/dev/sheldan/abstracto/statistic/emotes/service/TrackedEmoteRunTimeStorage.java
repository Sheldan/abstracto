package dev.sheldan.abstracto.statistic.emotes.service;

import dev.sheldan.abstracto.statistic.emotes.model.PersistingEmote;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TrackedEmoteRunTimeStorage {
    private HashMap<Long, Map<Long, List<PersistingEmote>>> trackedEmotes = new HashMap<>();

    public Map<Long, Map<Long, List<PersistingEmote>>> getRuntimeConfig() {
        return trackedEmotes;
    }

    public boolean contains(Long key) {
        return trackedEmotes.containsKey(key);
    }

    public void put(Long key, Map<Long, List<PersistingEmote>> objectToPut) {
        trackedEmotes.put(key, objectToPut);
    }

    public Map<Long, List<PersistingEmote>> get(Long key) {
        return trackedEmotes.get(key);
    }
}

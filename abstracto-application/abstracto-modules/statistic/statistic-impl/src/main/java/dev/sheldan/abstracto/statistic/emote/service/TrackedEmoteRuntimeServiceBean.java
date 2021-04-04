package dev.sheldan.abstracto.statistic.emote.service;

import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.statistic.emote.model.PersistingEmote;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class TrackedEmoteRuntimeServiceBean implements TrackedEmoteRuntimeService {

    @Autowired
    private TrackedEmoteRunTimeStorage trackedEmoteRunTimeStorage;
    private static final Lock runTimeLock = new ReentrantLock();

    @Override
    public Map<Long, Map<Long, List<PersistingEmote>>> getRuntimeConfig() {
        return trackedEmoteRunTimeStorage.getRuntimeConfig();
    }

    @Override
    public void addEmoteForServer(CachedEmote emote, Guild guild, boolean external) {
       addEmoteForServer(emote, guild, 1L, external);
    }

    @Override
    public void addEmoteForServer(CachedEmote emote, Guild guild, Long count, boolean external) {
        takeLock();
        try {
            // generate an appropriate key
            Long key = getKey();
            // create a PersistingEmote based the given Emote
            PersistingEmote newPersistentEmote = createFromEmote(guild, emote, count, external);
            if (trackedEmoteRunTimeStorage.contains(key)) {
                // if it already exists, we can add to the already existing map
                Map<Long, List<PersistingEmote>> elementsForKey = trackedEmoteRunTimeStorage.get(key);
                if (elementsForKey.containsKey(guild.getIdLong())) {
                    // if the server already has an entry, we can just add it to the list of existing ones
                    List<PersistingEmote> persistingEmotes = elementsForKey.get(guild.getIdLong());
                    Optional<PersistingEmote> existingEmote = persistingEmotes
                            .stream()
                            .filter(persistingEmote -> persistingEmote.getEmoteId().equals(emote.getEmoteId()))
                            .findFirst();
                    // if it exists already, just increment the counter by the given amount
                    existingEmote.ifPresent(persistingEmote -> persistingEmote.setCount(persistingEmote.getCount() + count));
                    if (!existingEmote.isPresent()) {
                        // just add the newly created one
                        persistingEmotes.add(newPersistentEmote);
                    }
                } else {
                    // it did not exist for the server, create a new list of PersistingEmote
                    log.debug("Adding emote {} to list of server {}.", newPersistentEmote.getEmoteId(), guild.getIdLong());
                    elementsForKey.put(guild.getIdLong(), new ArrayList<>(Arrays.asList(newPersistentEmote)));
                }
            } else {
                // no entry for the minute exists yet, add a new one
                HashMap<Long, List<PersistingEmote>> serverEmotes = new HashMap<>();
                serverEmotes.put(guild.getIdLong(), new ArrayList<>(Arrays.asList(newPersistentEmote)));
                log.debug("Adding emote map entry for server {}.", guild.getIdLong());
                trackedEmoteRunTimeStorage.put(key, serverEmotes);
            }
        } finally {
            releaseLock();
        }
    }

    @Override
    public Long getKey() {
        return Instant.now().getEpochSecond() / 60;
    }

    @Override
    public PersistingEmote createFromEmote(Guild guild, CachedEmote emote, boolean external) {
        return createFromEmote(guild, emote, 1L, external);
    }

    @Override
    public PersistingEmote createFromEmote(Guild guild, CachedEmote emote, Long count, boolean external) {
        String url = external ? emote.getImageURL() : null;
        return PersistingEmote
                .builder()
                .animated(emote.getAnimated())
                .emoteId(emote.getEmoteId())
                .external(external)
                .externalUrl(url)
                .emoteName(emote.getEmoteName())
                .count(count)
                .serverId(guild.getIdLong())
                .build();
    }

    @Override
    public void takeLock() {
        runTimeLock.lock();
    }

    @Override
    public void releaseLock() {
        runTimeLock.unlock();
    }

}

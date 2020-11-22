package dev.sheldan.abstracto.statistic.emotes.service;

import dev.sheldan.abstracto.statistic.emotes.model.PersistingEmote;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
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
    public void addEmoteForServer(Emote emote, Guild guild, boolean external) {
       addEmoteForServer(emote, guild, 1L, external);
    }

    @Override
    public void addEmoteForServer(Emote emote, Guild guild, Long count, boolean external) {
        takeLock();
        try {
            Long key = getKey();
            PersistingEmote newPersistentEmote = createFromEmote(guild, emote, count, external);
            if (trackedEmoteRunTimeStorage.contains(key)) {
                Map<Long, List<PersistingEmote>> elementsForKey = trackedEmoteRunTimeStorage.get(key);
                if (elementsForKey.containsKey(guild.getIdLong())) {
                    List<PersistingEmote> persistingEmotes = elementsForKey.get(guild.getIdLong());
                    Optional<PersistingEmote> existingEmote = persistingEmotes
                            .stream()
                            .filter(persistingEmote -> persistingEmote.getEmoteId().equals(emote.getIdLong()))
                            .findFirst();
                    existingEmote.ifPresent(persistingEmote -> persistingEmote.setCount(persistingEmote.getCount() + count));
                    if (!existingEmote.isPresent()) {
                        persistingEmotes.add(newPersistentEmote);
                    }
                } else {
                    log.trace("Adding emote {} to list of server {}.", newPersistentEmote.getEmoteId(), guild.getIdLong());
                    elementsForKey.put(guild.getIdLong(), new ArrayList<>(Arrays.asList(newPersistentEmote)));
                }
            } else {
                HashMap<Long, List<PersistingEmote>> serverEmotes = new HashMap<>();
                serverEmotes.put(guild.getIdLong(), new ArrayList<>(Arrays.asList(newPersistentEmote)));
                log.trace("Adding emote map entry for server {}.", guild.getIdLong());
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
    public PersistingEmote createFromEmote(Guild guild, Emote emote, boolean external) {
        return createFromEmote(guild, emote, 1L, external);
    }

    @Override
    public PersistingEmote createFromEmote(Guild guild, Emote emote, Long count, boolean external) {
        String url = external ? emote.getImageUrl() : null;
        return PersistingEmote
                .builder()
                .animated(emote.isAnimated())
                .emoteId(emote.getIdLong())
                .external(external)
                .externalUrl(url)
                .emoteName(emote.getName())
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

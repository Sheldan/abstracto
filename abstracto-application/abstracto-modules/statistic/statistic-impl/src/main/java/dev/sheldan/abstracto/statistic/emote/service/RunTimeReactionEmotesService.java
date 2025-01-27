package dev.sheldan.abstracto.statistic.emote.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Getter
@Slf4j
public class RunTimeReactionEmotesService {

    // key pattern "serverId:userId:messageId:emoteId"
    private final Map<String, Instant> runtimeEmotes = new ConcurrentHashMap<>();

    public void cleanupRunTimeStorage() {

        Instant cutoffDate = Instant.now().minus(2, ChronoUnit.DAYS);
        Set<String> keysToRemove = new HashSet<>();
        runtimeEmotes.forEach((key, instant) -> {
            if(instant.isBefore(cutoffDate)) {
                keysToRemove.add(key);
            }
        });
        log.info("Cleaning up {} emote usages.", keysToRemove.size());
        keysToRemove.forEach(runtimeEmotes::remove);
    }

    public boolean emoteAlreadyUsed(Long guildId,  Long userId, Long messageId, Long emoteId) {
        return runtimeEmotes.containsKey(getKeyFormat(guildId, userId, messageId, emoteId));
    }

    public String getKeyFormat(Long guildId, Long userId, Long messageId, Long emoteId) {
        return String.format("%s:%s:%s:%s", guildId, userId, messageId, emoteId);
    }
}
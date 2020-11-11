package dev.sheldan.abstracto.statistic.emotes.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsResult;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.embed.UsedEmoteDay;
import dev.sheldan.abstracto.statistic.emotes.repository.UsedEmoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class UsedEmoteManagementServiceBean implements UsedEmoteManagementService {

    @Autowired
    private UsedEmoteRepository usedEmoteRepository;

    @Override
    public Optional<UsedEmote> loadUsedEmoteForTrackedEmoteToday(TrackedEmote trackedEmote) {
        return usedEmoteRepository.findEmoteFromServerToday(trackedEmote.getTrackedEmoteId().getEmoteId() , trackedEmote.getTrackedEmoteId().getServerId());
    }

    @Override
    public UsedEmote createEmoteUsageForToday(TrackedEmote trackedEmote, Long count) {
        UsedEmote usedEmote = UsedEmote
                .builder()
                .emoteId(new UsedEmoteDay(trackedEmote.getTrackedEmoteId().getEmoteId(), trackedEmote.getTrackedEmoteId().getServerId(), Instant.now()))
                .amount(count)
                .build();
        log.trace("Creating emote usage for emote {} in server {} with count {}.", trackedEmote.getTrackedEmoteId().getEmoteId(), trackedEmote.getTrackedEmoteId().getServerId(), count);
        return usedEmoteRepository.save(usedEmote);
    }

    @Override
    public List<UsedEmote> loadEmoteUsagesForServerSince(AServer server, Instant since) {
        return usedEmoteRepository.getByEmoteId_ServerIdAndEmoteId_UseDateGreaterThan(server.getId(), since);
    }

    @Override
    public List<EmoteStatsResult> loadAllEmoteStatsForServerSince(AServer server, Instant since) {
        return usedEmoteRepository.getEmoteStatsForServerSince(server.getId(), since);
    }

    @Override
    public List<EmoteStatsResult> loadDeletedEmoteStatsForServerSince(AServer server, Instant since) {
        return usedEmoteRepository.getDeletedEmoteStatsForServerSince(server.getId(), since);
    }

    @Override
    public List<EmoteStatsResult> loadExternalEmoteStatsForServerSince(AServer server, Instant since) {
        return usedEmoteRepository.getExternalEmoteStatsForServerSince(server.getId(), since);
    }

    @Override
    public List<EmoteStatsResult> loadActiveEmoteStatsForServerSince(AServer server, Instant since) {
        return usedEmoteRepository.getCurrentlyExistingEmoteStatsForServerSince(server.getId(), since);
    }

    @Override
    public void purgeEmoteUsagesSince(TrackedEmote emote, Instant since) {
        usedEmoteRepository.deleteByEmoteId_EmoteIdAndEmoteId_ServerIdAndEmoteId_UseDateGreaterThan(emote.getTrackedEmoteId().getEmoteId(), emote.getTrackedEmoteId().getServerId(), since);
    }
}

package dev.sheldan.abstracto.statistic.emote.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.statistic.emote.model.EmoteStatsResult;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.model.database.UsedEmote;
import dev.sheldan.abstracto.statistic.emote.model.database.UsedEmoteType;
import dev.sheldan.abstracto.statistic.emote.model.database.embed.UsedEmoteDay;
import dev.sheldan.abstracto.statistic.emote.repository.UsedEmoteRepository;
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
    public Optional<UsedEmote> loadUsedEmoteForTrackedEmoteToday(TrackedEmote trackedEmote, UsedEmoteType usedEmoteType) {
        return usedEmoteRepository.findEmoteFromServerToday(trackedEmote.getTrackedEmoteId().getId(), trackedEmote.getTrackedEmoteId().getServerId(), usedEmoteType.name());
    }

    @Override
    public UsedEmote createEmoteUsageForToday(TrackedEmote trackedEmote, Long count, UsedEmoteType type) {
        return createEmoteUsageFor(trackedEmote, count, Instant.now(), type);
    }

    @Override
    public UsedEmote createEmoteUsageFor(TrackedEmote trackedEmote, Long count, Instant instant, UsedEmoteType type) {
        UsedEmote usedEmote = UsedEmote
                .builder()
                .emoteId(new UsedEmoteDay(trackedEmote.getTrackedEmoteId().getId(), trackedEmote.getTrackedEmoteId().getServerId(), instant, type))
                .amount(count)
                .build();
        log.debug("Creating emote usage for emote {} in server {} with count {}.", trackedEmote.getTrackedEmoteId().getId(), trackedEmote.getTrackedEmoteId().getServerId(), count);
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
    public List<EmoteStatsResult> loadDeletedEmoteStatsForServerSince(AServer server, Instant since, UsedEmoteType usedEmoteType) {
        if (usedEmoteType == null) {
            return usedEmoteRepository.getDeletedEmoteStatsForServerSince(server.getId(), since);
        } else {
            return usedEmoteRepository.getDeletedEmoteStatsForServerSince(server.getId(), since, usedEmoteType.name());
        }
    }

    @Override
    public List<EmoteStatsResult> loadExternalEmoteStatsForServerSince(AServer server, Instant since, UsedEmoteType type) {
        if (type == null) {
            return usedEmoteRepository.getExternalEmoteStatsForServerSince(server.getId(), since);
        } else {
            return usedEmoteRepository.getExternalEmoteStatsForServerSince(server.getId(), since, type.name());
        }
    }

    @Override
    public List<EmoteStatsResult> loadActiveEmoteStatsForServerSince(AServer server, Instant since, UsedEmoteType usedEmoteType) {
        if(usedEmoteType == null) {
            return usedEmoteRepository.getCurrentlyExistingEmoteStatsForServerSince(server.getId(), since);
        } else {
            return usedEmoteRepository.getCurrentlyExistingEmoteStatsForServerSince(server.getId(), since, usedEmoteType.name());
        }
    }

    @Override
    public EmoteStatsResult loadEmoteStatForEmote(TrackedEmote trackedEmote, Instant since, UsedEmoteType usedEmoteType) {
        if(usedEmoteType == null) {
            return usedEmoteRepository.getEmoteStatForTrackedEmote(trackedEmote.getTrackedEmoteId().getId(), trackedEmote.getTrackedEmoteId().getServerId(), since);
        } else {
            return usedEmoteRepository.getEmoteStatForTrackedEmote(trackedEmote.getTrackedEmoteId().getId(), trackedEmote.getTrackedEmoteId().getServerId(), since, usedEmoteType.name());
        }
    }

    @Override
    public void purgeEmoteUsagesSince(TrackedEmote emote, Instant since) {
        usedEmoteRepository.deleteByEmoteId_EmoteIdAndEmoteId_ServerIdAndEmoteId_UseDateGreaterThan(emote.getTrackedEmoteId().getId(), emote.getTrackedEmoteId().getServerId(), since);
    }
}

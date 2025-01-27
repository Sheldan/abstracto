package dev.sheldan.abstracto.statistic.emote.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.statistic.emote.converter.EmoteStatsConverter;
import dev.sheldan.abstracto.statistic.emote.model.EmoteStatsModel;
import dev.sheldan.abstracto.statistic.emote.model.EmoteStatsResult;
import dev.sheldan.abstracto.statistic.emote.model.EmoteStatsResultDisplay;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.model.database.UsedEmoteType;
import dev.sheldan.abstracto.statistic.emote.service.management.UsedEmoteManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Slf4j
public class UsedEmoteServiceBean implements UsedEmoteService {

    @Autowired
    private EmoteStatsConverter converter;

    @Autowired
    private UsedEmoteManagementService usedEmoteManagementService;

    @Override
    public EmoteStatsModel getEmoteStatsForServerSince(AServer server, Instant since) {
        List<EmoteStatsResult> emoteStatsResults = usedEmoteManagementService.loadAllEmoteStatsForServerSince(server, since);
        return converter.fromEmoteStatsResults(emoteStatsResults);
    }

    @Override
    public EmoteStatsModel getDeletedEmoteStatsForServerSince(AServer server, Instant since, UsedEmoteType usedEmoteType) {
        List<EmoteStatsResult> emoteStatsResults = usedEmoteManagementService.loadDeletedEmoteStatsForServerSince(server, since, usedEmoteType);
        return converter.fromEmoteStatsResults(emoteStatsResults);
    }

    @Override
    public EmoteStatsModel getExternalEmoteStatsForServerSince(AServer server, Instant since, UsedEmoteType type) {
        List<EmoteStatsResult> emoteStatsResults = usedEmoteManagementService.loadExternalEmoteStatsForServerSince(server, since, type);
        return converter.fromEmoteStatsResults(emoteStatsResults);
    }

    @Override
    public EmoteStatsModel getActiveEmoteStatsForServerSince(AServer server, Instant since, UsedEmoteType usedEmoteType) {
        List<EmoteStatsResult> emoteStatsResults = usedEmoteManagementService.loadActiveEmoteStatsForServerSince(server, since, usedEmoteType);
        return converter.fromEmoteStatsResults(emoteStatsResults);
    }

    @Override
    public EmoteStatsResultDisplay getEmoteStatForEmote(TrackedEmote trackedEmote, Instant since, UsedEmoteType usedEmoteType) {
        EmoteStatsResult emoteStatsResult = usedEmoteManagementService.loadEmoteStatForEmote(trackedEmote, since, usedEmoteType);
        return converter.convertEmoteStatsResultToDisplay(emoteStatsResult);
    }

    @Override
    public void purgeEmoteUsagesSince(TrackedEmote emote, Instant since) {
        log.info("Purging emote {} in server {} since {}.", emote.getTrackedEmoteId().getId(), emote.getTrackedEmoteId().getServerId(), since);
        usedEmoteManagementService.purgeEmoteUsagesSince(emote, since.truncatedTo(ChronoUnit.DAYS));
    }

    @Override
    public void purgeEmoteUsages(TrackedEmote emote) {
        purgeEmoteUsagesSince(emote, Instant.EPOCH);
    }
}

package dev.sheldan.abstracto.statistic.emotes.service;

import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emotes.model.PersistingEmote;
import dev.sheldan.abstracto.statistic.emotes.model.TrackedEmoteOverview;
import dev.sheldan.abstracto.statistic.emotes.model.TrackedEmoteSynchronizationResult;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.embed.TrackedEmoteServer;
import dev.sheldan.abstracto.statistic.emotes.service.management.TrackedEmoteManagementService;
import dev.sheldan.abstracto.statistic.emotes.service.management.UsedEmoteManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class TrackedEmoteServiceBean implements TrackedEmoteService {

    @Autowired
    private TrackedEmoteRuntimeService trackedEmoteRuntimeService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Autowired
    private UsedEmoteManagementService usedEmoteManagementService;

    @Autowired
    private BotService botService;

    @Override
    public void addEmoteToRuntimeStorage(List<Emote> emotes, Guild guild) {
        boolean externalTrackingEnabled = featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, guild.getIdLong(), EmoteTrackingMode.EXTERNAL_EMOTES);
        emotes.forEach(emote -> {
            boolean emoteIsFromGuild = emoteService.emoteIsFromGuild(emote, guild);
            if(externalTrackingEnabled || emoteIsFromGuild) {
                trackedEmoteRuntimeService.addEmoteForServer(emote, guild, !emoteIsFromGuild);
            }
        });
    }

    @Override
    public void addEmoteToRuntimeStorage(Emote emote, Guild guild, Long count) {
        boolean externalTrackingEnabled = featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, guild.getIdLong(), EmoteTrackingMode.EXTERNAL_EMOTES);
        boolean emoteIsFromGuild = emoteService.emoteIsFromGuild(emote, guild);
        if(externalTrackingEnabled || emoteIsFromGuild) {
            trackedEmoteRuntimeService.addEmoteForServer(emote, guild, count, !emoteIsFromGuild);
        }
    }

    @Override
    @Transactional
    public void storeEmoteStatistics(Map<Long, List<PersistingEmote>> usagesToStore) {
        usagesToStore.forEach((serverId, persistingEmotes) -> {
            log.info("Storing {} emotes for server {}.", persistingEmotes.size(), serverId);
            boolean autoTrackExternalEmotes = featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, serverId, EmoteTrackingMode.AUTO_TRACK_EXTERNAL);
            boolean trackExternalEmotes = featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, serverId, EmoteTrackingMode.EXTERNAL_EMOTES);
            persistingEmotes.forEach(persistingEmote -> {
                Optional<TrackedEmote> emoteOptional = trackedEmoteManagementService.loadByEmoteIdOptional(persistingEmote.getEmoteId(), serverId);
                emoteOptional.ifPresent(trackedEmote -> {
                    if(trackedEmote.getTrackingEnabled()) {
                        Optional<UsedEmote> existingUsedEmote = usedEmoteManagementService.loadUsedEmoteForTrackedEmoteToday(trackedEmote);
                        existingUsedEmote.ifPresent(usedEmote ->
                            usedEmote.setAmount(usedEmote.getAmount() + persistingEmote.getCount())
                        );
                        if(!existingUsedEmote.isPresent()) {
                            usedEmoteManagementService.createEmoteUsageForToday(trackedEmote, persistingEmote.getCount());
                        }
                    } else {
                        log.trace("Tracking disabled for emote {} in server {}.", trackedEmote.getTrackedEmoteId().getEmoteId(), trackedEmote.getTrackedEmoteId().getServerId());
                    }
                });
                if(!emoteOptional.isPresent()) {
                    createNewTrackedEmote(serverId, autoTrackExternalEmotes, trackExternalEmotes, persistingEmote);
                }
            });
        });
    }

    private void createNewTrackedEmote(Long serverId, boolean autoTrackExternalEmotes, boolean trackExternalEmotes, PersistingEmote persistingEmote) {
        Optional<Guild> guildOptional = botService.getGuildByIdOptional(serverId);
        guildOptional.ifPresent(guild -> {
            Emote emoteFromGuild = guild.getEmoteById(persistingEmote.getEmoteId());
            if(emoteFromGuild != null) {
                TrackedEmote newCreatedTrackedEmote = trackedEmoteManagementService.createTrackedEmote(emoteFromGuild, guild);
                usedEmoteManagementService.createEmoteUsageForToday(newCreatedTrackedEmote, persistingEmote.getCount());
            } else if(autoTrackExternalEmotes && trackExternalEmotes){
                TrackedEmote newCreatedTrackedEmote = trackedEmoteManagementService.createExternalEmote(persistingEmote);
                usedEmoteManagementService.createEmoteUsageForToday(newCreatedTrackedEmote, persistingEmote.getCount());
            }
        });
    }

    @Override
    public TrackedEmote getFakeTrackedEmote(Emote emote, Guild guild) {
        return getFakeTrackedEmote(emote.getIdLong(), guild);
    }

    @Override
    public TrackedEmote getFakeTrackedEmote(Long id, Guild guild) {
        return TrackedEmote
                .builder()
                .trackedEmoteId(new TrackedEmoteServer(id, guild.getIdLong()))
                .fake(true)
                .build();
    }

    @Override
    public TrackedEmoteSynchronizationResult synchronizeTrackedEmotes(Guild guild) {
        List<TrackedEmote> activeTrackedEmotes = trackedEmoteManagementService.getAllActiveTrackedEmoteForServer(guild.getIdLong());
        Long addedEmotes = 0L;
        List<Emote> allExistingEmotes = guild.getEmotes();
        log.info("Synchronizing emotes for server {}, currently tracked emotes {}, available emotes for server {}.", guild.getIdLong(), activeTrackedEmotes.size(), allExistingEmotes.size());
        for (Emote emote : allExistingEmotes) {
            Optional<TrackedEmote> trackedEmoteOptional = activeTrackedEmotes
                    .stream()
                    .filter(trackedEmote ->
                            trackedEmote.getTrackedEmoteId().getEmoteId().equals(emote.getIdLong())
                                    && trackedEmote.getTrackedEmoteId().getServerId().equals(guild.getIdLong()))
                    .findFirst();
            if (!trackedEmoteOptional.isPresent()) {
                trackedEmoteManagementService.createTrackedEmote(emote, guild);
                addedEmotes++;
            } else {
                activeTrackedEmotes.remove(trackedEmoteOptional.get());
            }
        }

        activeTrackedEmotes.forEach(trackedEmote ->
            trackedEmoteManagementService.markAsDeleted(trackedEmote)
        );
        return TrackedEmoteSynchronizationResult
                .builder()
                .emotesAdded(addedEmotes)
                .emotesMarkedDeleted((long) activeTrackedEmotes.size())
                .build();
    }

    @Override
    public TrackedEmoteOverview loadTrackedEmoteOverview(Guild guild) {
        return loadTrackedEmoteOverview(guild, false);
    }

    @Override
    public TrackedEmoteOverview loadTrackedEmoteOverview(Guild guild, Boolean showTrackingDisabled) {
        List<TrackedEmote> trackedEmotes = trackedEmoteManagementService.getTrackedEmoteForServer(guild.getIdLong(), showTrackingDisabled);
        TrackedEmoteOverview emoteOverView = TrackedEmoteOverview.builder().build();
        trackedEmotes.forEach(trackedEmote ->
            emoteOverView.addTrackedEmote(trackedEmote, guild)
        );
        return emoteOverView;
    }

    @Override
    public TrackedEmote createFakeTrackedEmote(Emote emote, Guild guild) {
        boolean external = !emoteService.emoteIsFromGuild(emote, guild);
        return trackedEmoteManagementService.createTrackedEmote(emote, guild, external);
    }

    @Override
    public void deleteTrackedEmote(TrackedEmote trackedEmote) {
        usedEmoteManagementService.purgeEmoteUsagesSince(trackedEmote, Instant.EPOCH);
        trackedEmoteManagementService.deleteTrackedEmote(trackedEmote);
    }

    @Override
    public void resetEmoteStats(Guild guild) {
        List<TrackedEmote> trackedEmotes = trackedEmoteManagementService.getTrackedEmoteForServer(guild.getIdLong(), true);
        trackedEmotes.forEach(this::deleteTrackedEmote);
    }

    @Override
    public void disableEmoteTracking(Guild guild) {
        List<TrackedEmote> trackedEmotes = trackedEmoteManagementService.getTrackedEmoteForServer(guild.getIdLong(), true);
        trackedEmotes.forEach(trackedEmote -> trackedEmote.setTrackingEnabled(false));
    }

}

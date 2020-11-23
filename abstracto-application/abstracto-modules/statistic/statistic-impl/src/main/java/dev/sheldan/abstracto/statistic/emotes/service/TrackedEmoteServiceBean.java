package dev.sheldan.abstracto.statistic.emotes.service;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
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
            // either the emote is from the current guild (we always add those) or external emote tracking is enabled (we should always add those)
            if(externalTrackingEnabled || emoteIsFromGuild) {
                trackedEmoteRuntimeService.addEmoteForServer(emote, guild, !emoteIsFromGuild);
            }
        });
    }

    @Override
    public void addEmoteToRuntimeStorage(Emote emote, Guild guild, Long count) {
        boolean externalTrackingEnabled = featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, guild.getIdLong(), EmoteTrackingMode.EXTERNAL_EMOTES);
        boolean emoteIsFromGuild = emoteService.emoteIsFromGuild(emote, guild);
        // either the emote is from the current guild (we always add those) or external emote tracking is enabled (we should always add those)
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
                    // only track the record, if its enabled
                    if(trackedEmote.getTrackingEnabled()) {
                        Optional<UsedEmote> existingUsedEmote = usedEmoteManagementService.loadUsedEmoteForTrackedEmoteToday(trackedEmote);
                        // if a use for today already exists, increment the amount
                        existingUsedEmote.ifPresent(usedEmote ->
                            usedEmote.setAmount(usedEmote.getAmount() + persistingEmote.getCount())
                        );
                        // if none exists, create a new
                        if(!existingUsedEmote.isPresent()) {
                            usedEmoteManagementService.createEmoteUsageForToday(trackedEmote, persistingEmote.getCount());
                        }
                    } else {
                        log.trace("Tracking disabled for emote {} in server {}.", trackedEmote.getTrackedEmoteId().getId(), trackedEmote.getTrackedEmoteId().getServerId());
                    }
                });
                // if tracked emote does not exists, we might want to create one (only for external emotes)
                // we only do it for external emotes, because the feature mode AUTO_TRACK would not make sense
                // we might want emotes which are completely ignored by emote tracking
                if(!emoteOptional.isPresent() && autoTrackExternalEmotes && trackExternalEmotes) {
                    createNewTrackedEmote(serverId, persistingEmote);
                }
            });
        });
    }

    /**
     * Creates a new {@link TrackedEmote} from the given {@link PersistingEmote}.
     * @param serverId The ID of the {@link dev.sheldan.abstracto.core.models.database.AServer} for which the {@link TrackedEmote} should be created for
     * @param persistingEmote The {@link PersistingEmote} which contains all information necessary to create a {@link TrackedEmote}
     */
    private void createNewTrackedEmote(Long serverId, PersistingEmote persistingEmote) {
        Optional<Guild> guildOptional = botService.getGuildByIdOptional(serverId);
        guildOptional.ifPresent(guild -> {
            TrackedEmote newCreatedTrackedEmote = trackedEmoteManagementService.createExternalTrackedEmote(persistingEmote);
            usedEmoteManagementService.createEmoteUsageForToday(newCreatedTrackedEmote, persistingEmote.getCount());
        });
    }

    @Override
    public TrackedEmote getFakeTrackedEmote(Emote emote, Guild guild) {
        return getFakeTrackedEmote(emote.getIdLong(), guild);
    }

    @Override
    public TrackedEmote getFakeTrackedEmote(Long emoteId, Guild guild) {
        return TrackedEmote
                .builder()
                .trackedEmoteId(new ServerSpecificId(guild.getIdLong(), emoteId))
                .fake(true)
                .build();
    }

    @Override
    public TrackedEmoteSynchronizationResult synchronizeTrackedEmotes(Guild guild) {
        List<TrackedEmote> activeTrackedEmotes = trackedEmoteManagementService.getAllActiveTrackedEmoteForServer(guild.getIdLong());
        Long addedEmotes = 0L;
        List<Emote> allExistingEmotes = guild.getEmotes();
        log.info("Synchronizing emotes for server {}, currently tracked emotes {}, available emotes for server {}.", guild.getIdLong(), activeTrackedEmotes.size(), allExistingEmotes.size());
        // iterate over all emotes currently available in the guild
        for (Emote emote : allExistingEmotes) {
            // find the emote in the list of known TrackedEmote
            Optional<TrackedEmote> trackedEmoteOptional = activeTrackedEmotes
                    .stream()
                    .filter(trackedEmote ->
                            trackedEmote.getTrackedEmoteId().getId().equals(emote.getIdLong())
                                    && trackedEmote.getTrackedEmoteId().getServerId().equals(guild.getIdLong()))
                    .findFirst();
            // if its not present, create it
            if (!trackedEmoteOptional.isPresent()) {
                trackedEmoteManagementService.createTrackedEmote(emote, guild);
                addedEmotes++;
            } else {
                // if we know it, remove it from the current tracked emotes
                activeTrackedEmotes.remove(trackedEmoteOptional.get());
            }
        }

        // the ones which are still around here, were not found in the emotes retrieved from the guild, we can mark them as deleted
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
    public TrackedEmote createTrackedEmote(Emote emote, Guild guild) {
        boolean external = !emoteService.emoteIsFromGuild(emote, guild);
        return createTrackedEmote(emote, guild, external);
    }

    @Override
    public TrackedEmote createTrackedEmote(Emote emote, Guild guild, boolean external) {
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

package dev.sheldan.abstracto.statistic.emote.service;

import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.service.CacheEntityService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emote.model.PersistingEmote;
import dev.sheldan.abstracto.statistic.emote.model.TrackedEmoteOverview;
import dev.sheldan.abstracto.statistic.emote.model.TrackedEmoteSynchronizationResult;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.model.database.UsedEmote;
import dev.sheldan.abstracto.statistic.emote.model.database.UsedEmoteType;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
import dev.sheldan.abstracto.statistic.emote.service.management.UsedEmoteManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
    private GuildService guildService;

    @Autowired
    private MetricService metricService;

    @Autowired
    private CacheEntityService cacheEntityService;

    public static final String EMOTE_USAGES_TRACKED_METRIC = "emote.usages";
    private static final CounterMetric EMOTE_USAGES_TRACKED =
            CounterMetric
                    .builder()
                    .name(EMOTE_USAGES_TRACKED_METRIC)
                    .build();


    @Override
    public void addEmoteToRuntimeStorage(List<CachedEmote> emotes, Guild guild, UsedEmoteType type) {
        boolean externalTrackingEnabled = featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, guild.getIdLong(), EmoteTrackingMode.EXTERNAL_EMOTES);
        emotes.forEach(emote -> {
            // either the emote is from the current guild (we always add those) or external emote tracking is enabled (we should always add those)
            if(externalTrackingEnabled || !emote.getExternal()) {
                trackedEmoteRuntimeService.addEmoteForServer(emote, guild, 1L, emote.getExternal(), type);
            }
        });
    }


    @Override
    public void addEmoteToRuntimeStorage(CachedEmote emote, Guild guild, Long count, UsedEmoteType type) {
        boolean externalTrackingEnabled = featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, guild.getIdLong(), EmoteTrackingMode.EXTERNAL_EMOTES);
        // either the emote is from the current guild (we always add those) or external emote tracking is enabled (we should always add those)
        if(externalTrackingEnabled || !emote.getExternal()) {
            trackedEmoteRuntimeService.addEmoteForServer(emote, guild, count, emote.getExternal(), type);
        }
    }

    @Override
    public void addEmoteToRuntimeStorage(CustomEmoji emote, Guild guild, Long count, UsedEmoteType type) {
        addEmoteToRuntimeStorage(cacheEntityService.getCachedEmoteFromEmote(emote, guild), guild, count, type);
    }

    @Override
    @Transactional
    public void storeEmoteStatistics(Map<Long, List<PersistingEmote>> usagesToStore) {
        usagesToStore.forEach((serverId, persistingEmotes) -> {
            log.info("Storing {} emotes for server {}.", persistingEmotes.size(), serverId);
            boolean autoTrackExternalEmotes = featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, serverId, EmoteTrackingMode.AUTO_TRACK_EXTERNAL);
            boolean trackExternalEmotes = featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, serverId, EmoteTrackingMode.EXTERNAL_EMOTES);
            persistingEmotes.forEach(persistingEmote -> {
                metricService.incrementCounter(EMOTE_USAGES_TRACKED);
                Optional<TrackedEmote> emoteOptional = trackedEmoteManagementService.loadByEmoteIdOptional(persistingEmote.getEmoteId(), serverId);
                emoteOptional.ifPresent(trackedEmote -> {
                    // only track the record, if its enabled
                    if(trackedEmote.getTrackingEnabled()) {
                        Optional<UsedEmote> existingUsedEmote = usedEmoteManagementService.loadUsedEmoteForTrackedEmoteToday(trackedEmote, persistingEmote.getUsedEmoteType());
                        // if a use for today already exists, increment the amount
                        existingUsedEmote.ifPresent(usedEmote ->
                            usedEmote.setAmount(usedEmote.getAmount() + persistingEmote.getCount())
                        );
                        // if none exists, create a new
                        if(existingUsedEmote.isEmpty()) {
                            usedEmoteManagementService.createEmoteUsageForToday(trackedEmote, persistingEmote.getCount(), persistingEmote.getUsedEmoteType());
                        }
                    } else {
                        log.debug("Tracking disabled for emote {} in server {}.", trackedEmote.getTrackedEmoteId().getId(), trackedEmote.getTrackedEmoteId().getServerId());
                    }
                });
                // if tracked emote does not exist, we might want to create one (only for external emotes)
                // we only do it for external emotes, because the feature mode AUTO_TRACK would not make sense
                // we might want emotes which are completely ignored by emote tracking
                if(emoteOptional.isEmpty() && autoTrackExternalEmotes && trackExternalEmotes) {
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
        Optional<Guild> guildOptional = guildService.getGuildByIdOptional(serverId);
        guildOptional.ifPresent(guild -> {
            TrackedEmote newCreatedTrackedEmote = trackedEmoteManagementService.createExternalTrackedEmote(persistingEmote);
            usedEmoteManagementService.createEmoteUsageForToday(newCreatedTrackedEmote, persistingEmote.getCount(), persistingEmote.getUsedEmoteType());
        });
    }

    @Override
    public TrackedEmote getFakeTrackedEmote(CustomEmoji emote, Guild guild) {
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
        List<RichCustomEmoji> allExistingEmotes = guild.getEmojis();
        log.info("Synchronizing emotes for server {}, currently tracked emotes {}, available emotes for server {}.", guild.getIdLong(), activeTrackedEmotes.size(), allExistingEmotes.size());
        // iterate over all emotes currently available in the guild
        for (RichCustomEmoji emote : allExistingEmotes) {
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
    public TrackedEmote createTrackedEmote(CustomEmoji emote, Guild guild) {
        boolean external = !emoteService.emoteIsFromGuild(emote, guild);
        return createTrackedEmote(emote, guild, external);
    }

    @Override
    public TrackedEmote createTrackedEmote(CustomEmoji emote, Guild guild, boolean external) {
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

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(EMOTE_USAGES_TRACKED, "Tracked emote usages stored");
    }
}

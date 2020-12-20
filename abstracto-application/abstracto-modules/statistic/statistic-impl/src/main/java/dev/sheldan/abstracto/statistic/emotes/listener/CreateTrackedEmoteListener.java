package dev.sheldan.abstracto.statistic.emotes.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncEmoteCreatedListener;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emotes.service.management.TrackedEmoteManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * This listener listens for created {@link Emote} in a {@link net.dv8tion.jda.api.entities.Guild} and creates appropriate
 * {@link dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote}, if the EMOTE_TRACKING feature is enabled and the AUTO_TRACK
 * feature mode as well.
 */
@Component
@Slf4j
public class CreateTrackedEmoteListener implements AsyncEmoteCreatedListener {

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Override
    public void emoteCreated(CachedEmote createdEmote) {
        log.info("Creating tracked emote {} in server {}.", createdEmote.getServerId(), createdEmote.getEmoteId());
        trackedEmoteManagementService.createTrackedEmote(createdEmote);
    }

    @Override
    public FeatureEnum getFeature() {
        return StatisticFeatures.EMOTE_TRACKING;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(EmoteTrackingMode.AUTO_TRACK);
    }
}

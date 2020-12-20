package dev.sheldan.abstracto.statistic.emotes.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncEmoteUpdatedListener;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.service.management.TrackedEmoteManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * This listener listens for emote name changes and appropriately updates the name of the {@link TrackedEmote} in the database,
 * if the emote is tracked. This is only executed if the EMOTE_TRACKING feature is enabled,and if the AUTO_TRACK feature mode is enabled.
 */
@Component
public class UpdateTrackedEmoteListener implements AsyncEmoteUpdatedListener {

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Override
    public void emoteUpdated(CachedEmote updatedEmote, String oldValue, String newValue) {
        TrackedEmote trackedEmote = trackedEmoteManagementService.loadByEmoteId(updatedEmote.getEmoteId(), updatedEmote.getServerId());
        trackedEmoteManagementService.changeName(trackedEmote, newValue);
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

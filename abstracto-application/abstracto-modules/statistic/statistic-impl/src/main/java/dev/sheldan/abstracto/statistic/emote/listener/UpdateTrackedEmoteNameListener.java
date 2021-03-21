package dev.sheldan.abstracto.statistic.emote.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncEmoteNameUpdatedListener;
import dev.sheldan.abstracto.core.models.listener.EmoteNameUpdatedModel;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * This listener listens for emote name changes and appropriately updates the name of the {@link TrackedEmote} in the database,
 * if the emote is tracked. This is only executed if the EMOTE_TRACKING feature is enabled,and if the AUTO_TRACK feature mode is enabled.
 */
@Component
public class UpdateTrackedEmoteNameListener implements AsyncEmoteNameUpdatedListener {

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Override
    public FeatureDefinition getFeature() {
        return StatisticFeatureDefinition.EMOTE_TRACKING;
    }

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(EmoteTrackingMode.AUTO_TRACK);
    }

    @Override
    public DefaultListenerResult execute(EmoteNameUpdatedModel model) {
        TrackedEmote trackedEmote = trackedEmoteManagementService.loadByEmote(model.getEmote());
        trackedEmoteManagementService.changeName(trackedEmote, model.getNewValue());
        return DefaultListenerResult.PROCESSED;
    }
}

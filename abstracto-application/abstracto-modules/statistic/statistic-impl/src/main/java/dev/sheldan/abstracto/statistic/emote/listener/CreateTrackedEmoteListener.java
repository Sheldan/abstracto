package dev.sheldan.abstracto.statistic.emote.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncEmoteCreatedListener;
import dev.sheldan.abstracto.core.models.listener.EmoteCreatedModel;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * This listener listens for created {@link Emote} in a {@link net.dv8tion.jda.api.entities.Guild} and creates appropriate
 * {@link dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote}, if the EMOTE_TRACKING feature is enabled and the AUTO_TRACK
 * feature mode as well.
 */
@Component
@Slf4j
public class CreateTrackedEmoteListener implements AsyncEmoteCreatedListener {

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
    public DefaultListenerResult execute(EmoteCreatedModel model) {
        log.info("Creating tracked emote {} in server {}.", model.getServerId(), model.getEmote().getIdLong());
        trackedEmoteManagementService.createTrackedEmote(model.getEmote());
        return DefaultListenerResult.PROCESSED;
    }
}

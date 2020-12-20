package dev.sheldan.abstracto.statistic.emotes.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This listener listens to every received message, if the EMOTE_TRACKING feature is enabled, and stores *all* used emotes in
 * the runtime storage for emote tracking.
 */
@Component
public class EmoteTrackingListener implements AsyncMessageReceivedListener {

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Autowired
    private BotService botService;

    @Override
    public void execute(CachedMessage message) {
        Map<Long, List<CachedEmote>> collect = message.getEmotes().stream().collect(Collectors.groupingBy(CachedEmote::getEmoteId));
        collect.values().forEach(groupedEmotes ->
            trackedEmoteService.addEmoteToRuntimeStorage(groupedEmotes.get(0), botService.getGuildById(message.getServerId()), (long) groupedEmotes.size())
        );
    }

    @Override
    public FeatureEnum getFeature() {
        return StatisticFeatures.EMOTE_TRACKING;
    }

}

package dev.sheldan.abstracto.statistic.emote.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Emote;
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
    private GuildService guildService;

    @Override
    public FeatureDefinition getFeature() {
        return StatisticFeatureDefinition.EMOTE_TRACKING;
    }

    @Override
    public DefaultListenerResult execute(MessageReceivedModel model) {
        Map<Long, List<Emote>> collect = model.getMessage().getEmotesBag().stream().collect(Collectors.groupingBy(Emote::getIdLong));
        collect.values().forEach(groupedEmotes ->
            trackedEmoteService.addEmoteToRuntimeStorage(groupedEmotes.get(0), guildService.getGuildById(model.getServerId()), (long) groupedEmotes.size())
        );
        return DefaultListenerResult.PROCESSED;
    }
}

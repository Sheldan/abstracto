package dev.sheldan.abstracto.statistic.emotes.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.MessageReceivedListener;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.collections4.Bag;
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
public class EmoteTrackingListener implements MessageReceivedListener {

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Override
    public void execute(Message message) {
        Bag<Emote> emotesBag = message.getEmotesBag();
        Map<Long, List<Emote>> collect = emotesBag.stream().collect(Collectors.groupingBy(ISnowflake::getIdLong));
        collect.values().forEach(groupedEmotes ->
            trackedEmoteService.addEmoteToRuntimeStorage(groupedEmotes.get(0), message.getGuild(), (long) groupedEmotes.size())
        );
    }

    @Override
    public FeatureEnum getFeature() {
        return StatisticFeatures.EMOTE_TRACKING;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.LOW;
    }
}

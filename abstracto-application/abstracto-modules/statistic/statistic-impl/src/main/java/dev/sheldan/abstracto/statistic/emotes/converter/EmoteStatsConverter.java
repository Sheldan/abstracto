package dev.sheldan.abstracto.statistic.emotes.converter;

import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsModel;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsResult;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsResultDisplay;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.service.management.TrackedEmoteManagementService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Component to convert from a {@link EmoteStatsResult} to a proper instance of {@link EmoteStatsModel}.
 * This for example loads the relevant {@link Emote} to be used within the model and also splits it up
 * into static and animated emotes
 */
@Component
public class EmoteStatsConverter {

    @Autowired
    private BotService botService;

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    public EmoteStatsModel fromEmoteStatsResults(List<EmoteStatsResult> resultList) {
        if(resultList.isEmpty()) {
            // no stats are available, do nothing
            return EmoteStatsModel.builder().build();
        }
        // it is assumed all emotes are tracked in the same server
        Guild relevantGuild = botService.getGuildById(resultList.get(0).getServerId());
        EmoteStatsModel resultingModel = EmoteStatsModel.builder().build();
        resultList.forEach(emoteStatsResult -> {
            TrackedEmote trackedEmote = trackedEmoteManagementService.loadByEmoteId(emoteStatsResult.getEmoteId(), emoteStatsResult.getServerId());
            Emote loadedEmote = null;
            // if the emote should still exist, we try to load it
            if(!trackedEmote.getExternal() && !trackedEmote.getDeleted()) {
                loadedEmote = relevantGuild.getEmoteById(trackedEmote.getTrackedEmoteId().getId());
            }
            EmoteStatsResultDisplay display = EmoteStatsResultDisplay
                    .builder()
                    .emote(loadedEmote)
                    .result(emoteStatsResult)
                    .trackedEmote(trackedEmote)
                    .build();
            if(display.getTrackedEmote().getAnimated()) {
                resultingModel.getAnimatedEmotes().add(display);
            } else {
                resultingModel.getStaticEmotes().add(display);
            }
        });
        return resultingModel;
    }
}

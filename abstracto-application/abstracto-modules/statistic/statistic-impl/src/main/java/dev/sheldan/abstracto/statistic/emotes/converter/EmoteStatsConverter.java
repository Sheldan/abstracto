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

@Component
public class EmoteStatsConverter {

    @Autowired
    private BotService botService;

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    public EmoteStatsModel fromEmoteStatsResults(List<EmoteStatsResult> resultList) {
        if(resultList.isEmpty()) {
            return EmoteStatsModel.builder().build();
        }
        Guild relevantGuild = botService.getGuildById(resultList.get(0).getServerId());
        EmoteStatsModel resultingModel = EmoteStatsModel.builder().build();
        resultList.forEach(emoteStatsResult -> {
            TrackedEmote trackedEmote = trackedEmoteManagementService.loadByEmoteId(emoteStatsResult.getEmoteId(), emoteStatsResult.getServerId());
            Emote loadedEmote = null;
            if(!trackedEmote.getExternal() && !trackedEmote.getDeleted()) {
                loadedEmote = relevantGuild.getEmoteById(trackedEmote.getTrackedEmoteId().getEmoteId());
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

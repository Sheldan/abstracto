package dev.sheldan.abstracto.statistic.emote.converter;

import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.statistic.emote.model.EmoteStatsModel;
import dev.sheldan.abstracto.statistic.emote.model.EmoteStatsResult;
import dev.sheldan.abstracto.statistic.emote.model.EmoteStatsResultDisplay;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Component to convert from a {@link EmoteStatsResult} to a proper instance of {@link EmoteStatsModel}.
 * This for example loads the relevant {@link net.dv8tion.jda.api.entities.emoji.CustomEmoji} to be used within the model and also splits it up
 * into static and animated emotes
 */
@Component
public class EmoteStatsConverter {

    @Autowired
    private GuildService guildService;

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    public EmoteStatsModel fromEmoteStatsResults(List<EmoteStatsResult> resultList) {
        if(resultList.isEmpty()) {
            // no stats are available, do nothing
            return EmoteStatsModel.builder().build();
        }
        // it is assumed all emotes are tracked in the same server
        Guild relevantGuild = guildService.getGuildById(resultList.get(0).getServerId());
        EmoteStatsModel resultingModel = EmoteStatsModel.builder().build();
        resultList.forEach(emoteStatsResult -> {
            EmoteStatsResultDisplay display = convertEmoteStatsResult(relevantGuild, emoteStatsResult);
            if(display.getTrackedEmote().getAnimated()) {
                resultingModel.getAnimatedEmotes().add(display);
            } else {
                resultingModel.getStaticEmotes().add(display);
            }
        });
        return resultingModel;
    }

    public EmoteStatsResultDisplay convertEmoteStatsResultToDisplay(EmoteStatsResult emoteStatsResult) {
        Guild relevantGuild = guildService.getGuildById(emoteStatsResult.getServerId());
        return convertEmoteStatsResult(relevantGuild, emoteStatsResult);
    }

    private EmoteStatsResultDisplay convertEmoteStatsResult(Guild relevantGuild, EmoteStatsResult emoteStatsResult) {
        TrackedEmote trackedEmote = trackedEmoteManagementService.loadByEmoteId(emoteStatsResult.getEmoteId(), emoteStatsResult.getServerId());
        CustomEmoji loadedEmote = null;
        // if the emote should still exist, we try to load it
        if(!trackedEmote.getExternal() && !trackedEmote.getDeleted()) {
            loadedEmote = relevantGuild.getEmojiById(trackedEmote.getTrackedEmoteId().getId());
        }
        return EmoteStatsResultDisplay
                .builder()
                .emote(loadedEmote)
                .result(emoteStatsResult)
                .trackedEmote(trackedEmote)
                .build();
    }
}

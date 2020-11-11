package dev.sheldan.abstracto.statistic.emotes.model;

import dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
public class DownloadEmoteStatsModel {
    private Guild guild;
    private Instant downloadDate;
    private Instant statsSince;
    private Member requester;
    private List<UsedEmote> emotes;
}

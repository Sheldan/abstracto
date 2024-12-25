package dev.sheldan.abstracto.giveaway.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.time.Duration;

@Builder
@Getter
public class GiveawayCreationRequest {

    private Long creatorId;
    private Long serverId;
    private Long benefactorId;
    private Long giveawayKeyId;
    private String title;
    private String description;
    private Duration duration;
    @Setter
    private GuildMessageChannel targetChannel;
    private Integer winnerCount;
}

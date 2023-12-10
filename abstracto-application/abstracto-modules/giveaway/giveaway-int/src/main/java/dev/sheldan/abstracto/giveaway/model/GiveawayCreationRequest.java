package dev.sheldan.abstracto.giveaway.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.time.Duration;

@Builder
@Getter
public class GiveawayCreationRequest {

    private Member creator;
    private Member benefactor;
    private String title;
    private String description;
    private Duration duration;
    @Setter
    private GuildMessageChannel targetChannel;
    private Integer winnerCount;
}

package dev.sheldan.abstracto.core.models.template.display;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

@Getter
@Setter
@Builder
public class ChannelDisplay {
    private String name;
    private String channelMention;

    public static ChannelDisplay fromChannel(TextChannel channel) {
        if(channel == null) {
            return null;
        }
        return ChannelDisplay
                .builder()
                .name(channel.getName())
                .channelMention(channel.getAsMention())
                .build();
    }

    public static ChannelDisplay fromChannel(GuildMessageChannel channel) {
        if(channel == null) {
            return null;
        }
        return ChannelDisplay
                .builder()
                .name(channel.getName())
                .channelMention(channel.getAsMention())
                .build();
    }}

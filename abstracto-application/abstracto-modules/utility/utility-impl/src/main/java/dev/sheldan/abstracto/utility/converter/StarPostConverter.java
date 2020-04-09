package dev.sheldan.abstracto.utility.converter;

import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.converter.ChannelConverter;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StarPostConverter {

    @Autowired
    private ChannelConverter channelConverter;

    public StarStatsPost fromStarboardPost(StarboardPost starboardPost) {
        ChannelDto channel = channelConverter.fromChannel(starboardPost.getStarboardChannel());
        return StarStatsPost
                .builder()
                .serverId(channel.getServer().getId())
                .channelId(channel.getId())
                .messageId(starboardPost.getPostMessageId())
                .starCount(starboardPost.getReactions().size())
                .build();
    }
}

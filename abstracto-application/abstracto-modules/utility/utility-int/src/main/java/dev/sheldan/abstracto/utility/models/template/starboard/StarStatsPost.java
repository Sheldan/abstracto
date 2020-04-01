package dev.sheldan.abstracto.utility.models.template.starboard;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.utils.MessageUtils;
import dev.sheldan.abstracto.utility.models.StarboardPost;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StarStatsPost {
    private Long serverId;
    private Long channelId;
    private Long messageId;
    private Integer starCount;

    public String getMessageUrl() {
        return MessageUtils.buildMessageUrl(serverId ,channelId, messageId);
    }

    public static StarStatsPost fromStarboardPost(StarboardPost starboardPost) {
        AChannel channel = starboardPost.getStarboardChannel();
        return StarStatsPost
                .builder()
                .serverId(channel.getServer().getId())
                .channelId(channel.getId())
                .messageId(starboardPost.getPostMessageId())
                .starCount(starboardPost.getReactions().size())
                .build();
    }
}

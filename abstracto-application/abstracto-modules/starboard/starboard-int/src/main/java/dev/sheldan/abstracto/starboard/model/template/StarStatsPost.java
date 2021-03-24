package dev.sheldan.abstracto.starboard.model.template;

import dev.sheldan.abstracto.core.utils.MessageUtils;
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
    private Long starCount;

    public String getMessageUrl() {
        return MessageUtils.buildMessageUrl(serverId ,channelId, messageId);
    }
}

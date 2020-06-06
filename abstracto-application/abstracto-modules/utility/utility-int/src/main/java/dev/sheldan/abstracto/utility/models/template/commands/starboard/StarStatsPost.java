package dev.sheldan.abstracto.utility.models.template.commands.starboard;

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
    private Integer starCount;

    public String getMessageUrl() {
        return MessageUtils.buildMessageUrl(serverId ,channelId, messageId);
    }
}

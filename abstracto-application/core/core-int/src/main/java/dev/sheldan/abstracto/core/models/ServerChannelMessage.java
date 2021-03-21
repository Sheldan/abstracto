package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.utils.MessageUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ServerChannelMessage {
    private Long serverId;
    private Long channelId;
    private Long messageId;

    public String getJumpUrl() {
        return MessageUtils.buildMessageUrl(serverId, channelId, messageId);
    }
}

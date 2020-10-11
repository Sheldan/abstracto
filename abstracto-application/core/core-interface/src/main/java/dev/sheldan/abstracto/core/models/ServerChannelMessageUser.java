package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ServerChannelMessageUser {
    private Long serverId;
    private Long channelId;
    private Long messageId;
    private Long userId;
}

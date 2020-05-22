package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AServerChannelUserId {
    private Long guildId;
    private Long channelId;
    private Long userId;
}

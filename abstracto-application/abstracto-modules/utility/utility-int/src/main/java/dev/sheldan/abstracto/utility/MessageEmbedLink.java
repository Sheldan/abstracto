package dev.sheldan.abstracto.utility;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageEmbedLink {
    private Long serverId;
    private Long channelId;
    private Long messageId;
    private String wholeUrl;
}

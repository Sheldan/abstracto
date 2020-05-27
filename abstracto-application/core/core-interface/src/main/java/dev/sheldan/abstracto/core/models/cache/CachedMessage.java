package dev.sheldan.abstracto.core.models.cache;

import dev.sheldan.abstracto.core.utils.MessageUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
public class CachedMessage {
    private Long serverId;
    private Long channelId;
    private Long messageId;
    private Long authorId;
    private Instant timeCreated;
    private String content;
    private List<CachedEmbed> embeds;
    private List<String> attachmentUrls;
    private List<CachedReaction> reactions;

    public String getMessageUrl() {
        return MessageUtils.buildMessageUrl(this.serverId ,this.channelId, this.messageId);
    }
}

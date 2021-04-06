package dev.sheldan.abstracto.core.models.cache;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.utils.MessageUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
public class CachedMessage {
    private Long serverId;
    private Long channelId;
    private Long messageId;
    private CachedAuthor author;
    private Instant timeCreated;
    private String content;
    private List<CachedEmbed> embeds;
    private List<CachedAttachment> attachments;
    private List<CachedReactions> reactions;
    private List<CachedEmote> emotes;
    private CachedMessage referencedMessage;

    public String getMessageUrl() {
        return MessageUtils.buildMessageUrl(this.serverId ,this.channelId, this.messageId);
    }

    public ServerUser getAuthorAsServerUser() {
        return ServerUser.builder().serverId(serverId).userId(author.getAuthorId()).build();
    }
}

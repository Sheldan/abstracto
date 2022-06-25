package dev.sheldan.abstracto.linkembed.model.template;

import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageEmbedDeleteButtonPayload implements ButtonPayload {
    private Long embeddingServerId;
    private Long embeddingChannelId;
    private Long embeddingMessageId;
    private Long embeddedServerId;
    private Long embeddedChannelId;
    private Long embeddedMessageId;
    private Long embeddedUserId;
    private Long embeddingUserId;
}

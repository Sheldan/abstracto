package dev.sheldan.abstracto.moderation.model.interaction;

import dev.sheldan.abstracto.core.interaction.modal.ModalPayload;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MessageReportModalPayload implements ModalPayload {
    private String modalId;
    private String textInputId;
    private Long messageId;
    private Long channelId;
    private Long serverId;
}

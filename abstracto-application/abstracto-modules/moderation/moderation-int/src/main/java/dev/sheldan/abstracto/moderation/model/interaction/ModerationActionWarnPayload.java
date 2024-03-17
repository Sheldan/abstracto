package dev.sheldan.abstracto.moderation.model.interaction;

import dev.sheldan.abstracto.core.interaction.modal.ModalPayload;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ModerationActionWarnPayload implements ModalPayload {
    private String modalId;
    private String reasonInputId;
    private Long serverId;
    private Long warnedUserId;
}

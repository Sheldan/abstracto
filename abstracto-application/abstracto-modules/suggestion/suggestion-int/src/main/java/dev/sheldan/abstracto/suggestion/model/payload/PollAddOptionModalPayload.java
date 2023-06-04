package dev.sheldan.abstracto.suggestion.model.payload;

import dev.sheldan.abstracto.core.interaction.modal.ModalPayload;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PollAddOptionModalPayload implements ModalPayload {
    private String modalId;
    private String labelInputComponentId;
    private String descriptionInputComponentId;
    private Long serverId;
    private Long pollId;
}

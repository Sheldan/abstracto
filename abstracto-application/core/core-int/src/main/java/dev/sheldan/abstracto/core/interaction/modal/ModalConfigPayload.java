package dev.sheldan.abstracto.core.interaction.modal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ModalConfigPayload {
    private String modalId;
    private ModalPayload modalPayload;
    private Class payloadType;
    private String origin;
}

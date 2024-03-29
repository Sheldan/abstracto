package dev.sheldan.abstracto.core.interaction.button;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ButtonConfigModel {
    private String buttonId;
    private ButtonPayload buttonPayload;
    private Class payloadType;
    private String origin;
}

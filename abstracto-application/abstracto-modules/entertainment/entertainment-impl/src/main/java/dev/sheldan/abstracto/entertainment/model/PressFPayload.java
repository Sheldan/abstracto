package dev.sheldan.abstracto.entertainment.model;

import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PressFPayload implements ButtonPayload {
    private Long pressFId;
}

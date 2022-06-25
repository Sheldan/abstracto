package dev.sheldan.abstracto.assignableroles.model;

import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AssignableRolePlacePayload implements ButtonPayload {
    private Long placeId;
    private Long roleId;
}

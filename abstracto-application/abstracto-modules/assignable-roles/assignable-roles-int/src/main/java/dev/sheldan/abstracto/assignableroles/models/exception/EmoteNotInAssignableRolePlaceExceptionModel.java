package dev.sheldan.abstracto.assignableroles.models.exception;

import dev.sheldan.abstracto.core.models.FullEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Builder
public class EmoteNotInAssignableRolePlaceExceptionModel implements Serializable {
    private final FullEmote emote;
    private final String placeName;
}

package dev.sheldan.abstracto.assignableroles.models.exception;

import dev.sheldan.abstracto.core.models.FullEmote;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class AssignableRoleAlreadyDefinedExceptionModel implements Serializable {
    private final FullEmote emote;
    private final String placeName;
}

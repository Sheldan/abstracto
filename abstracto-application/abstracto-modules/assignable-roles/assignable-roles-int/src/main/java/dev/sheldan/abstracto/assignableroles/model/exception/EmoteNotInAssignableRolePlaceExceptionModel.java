package dev.sheldan.abstracto.assignableroles.model.exception;

import dev.sheldan.abstracto.core.models.FullEmote;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Contains the model for {@link dev.sheldan.abstracto.assignableroles.exception.EmoteNotInAssignableRolePlaceException}
 */
@Getter
@Builder
public class EmoteNotInAssignableRolePlaceExceptionModel implements Serializable {
    /**
     * The {@link FullEmote emote} which was not found in the {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace place}
     */
    private final FullEmote emote;
    /**
     * The key of the {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace place} for which the emote was not found in
     */
    private final String placeName;
}

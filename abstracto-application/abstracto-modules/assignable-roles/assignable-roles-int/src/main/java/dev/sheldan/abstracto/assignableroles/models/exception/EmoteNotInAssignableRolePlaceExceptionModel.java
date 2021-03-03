package dev.sheldan.abstracto.assignableroles.models.exception;

import dev.sheldan.abstracto.core.models.FullEmote;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Contains the model for {@link dev.sheldan.abstracto.assignableroles.exceptions.EmoteNotInAssignableRolePlaceException}
 */
@Getter
@Builder
public class EmoteNotInAssignableRolePlaceExceptionModel implements Serializable {
    /**
     * The {@link FullEmote emote} which was not found in the {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace place}
     */
    private final FullEmote emote;
    /**
     * The key of the {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace place} for which the emote was not found in
     */
    private final String placeName;
}

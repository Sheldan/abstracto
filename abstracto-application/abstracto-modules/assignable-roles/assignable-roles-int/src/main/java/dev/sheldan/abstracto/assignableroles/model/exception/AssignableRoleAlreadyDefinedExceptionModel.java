package dev.sheldan.abstracto.assignableroles.model.exception;

import dev.sheldan.abstracto.core.models.FullEmote;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Contains the model for {@link dev.sheldan.abstracto.assignableroles.exception.AssignableRoleAlreadyDefinedException}
 */
@Getter
@Builder
public class AssignableRoleAlreadyDefinedExceptionModel implements Serializable {
    /**
     * The {@link FullEmote emote} which was tried to be added to a {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace place}
     */
    private final FullEmote emote;
    /**
     * The key of the {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace} for which it was tried to add a
     * {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRole role}
     */
    private final String placeName;
}

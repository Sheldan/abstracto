package dev.sheldan.abstracto.assignableroles.model.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Contains the model for {@link dev.sheldan.abstracto.assignableroles.exception.AssignableRolePlaceNotFoundException}
 */
@Getter
@Builder
public class AssignableRolePlaceNotFoundExceptionModel implements Serializable {
    /**
     * The ID of the {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace place} which was not found
     */
    private final Long placeId;
}

package dev.sheldan.abstracto.assignableroles.models.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Contains the model for {@link dev.sheldan.abstracto.assignableroles.exceptions.AssignableRolePlacePostNotFoundException}
 */
@Getter
@Builder
public class AssignableRolePlacePostNotFoundExceptionModel implements Serializable {
    /**
     * The ID of the {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost post} which was not found in the database
     */
    private final Long messageId;
}

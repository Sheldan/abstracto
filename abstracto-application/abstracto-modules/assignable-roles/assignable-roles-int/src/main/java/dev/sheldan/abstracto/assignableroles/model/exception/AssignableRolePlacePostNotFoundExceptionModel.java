package dev.sheldan.abstracto.assignableroles.model.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Contains the model for {@link dev.sheldan.abstracto.assignableroles.exception.AssignableRolePlacePostNotFoundException}
 */
@Getter
@Builder
public class AssignableRolePlacePostNotFoundExceptionModel implements Serializable {
    /**
     * The ID of the {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlacePost post} which was not found in the database
     */
    private final Long messageId;
}

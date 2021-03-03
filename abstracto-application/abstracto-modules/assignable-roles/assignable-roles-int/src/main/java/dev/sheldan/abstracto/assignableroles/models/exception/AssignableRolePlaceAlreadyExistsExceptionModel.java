package dev.sheldan.abstracto.assignableroles.models.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Contains the model for {@link dev.sheldan.abstracto.assignableroles.exceptions.AssignableRolePlaceAlreadyExistsException}
 */
@Getter
@Builder
public class AssignableRolePlaceAlreadyExistsExceptionModel implements Serializable {
    /**
     * The key which collides with an existing {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace}
     */
    private final String name;
}

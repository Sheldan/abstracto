package dev.sheldan.abstracto.assignableroles.models.exception;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Contains the model for {@link dev.sheldan.abstracto.assignableroles.exceptions.AssignedUserNotFoundException}
 */
@Getter
@Builder
public class AssignedUserNotFoundExceptionModel implements Serializable {
    /**
     * The instance of the {@link AUserInAServer userInAServer} for which the assigned user was not found
     */
    private final AUserInAServer aUserInAServer;
}

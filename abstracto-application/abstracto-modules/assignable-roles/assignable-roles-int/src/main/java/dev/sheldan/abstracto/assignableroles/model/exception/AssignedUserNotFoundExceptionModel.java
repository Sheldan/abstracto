package dev.sheldan.abstracto.assignableroles.model.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Contains the model for {@link dev.sheldan.abstracto.assignableroles.exception.AssignedUserNotFoundException}
 */
@Getter
@Builder
public class AssignedUserNotFoundExceptionModel implements Serializable {
    private final Long userId;
}

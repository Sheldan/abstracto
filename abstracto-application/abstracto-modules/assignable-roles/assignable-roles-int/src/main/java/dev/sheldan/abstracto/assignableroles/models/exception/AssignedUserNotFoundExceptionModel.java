package dev.sheldan.abstracto.assignableroles.models.exception;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class AssignedUserNotFoundExceptionModel implements Serializable {
    private final AUserInAServer aUserInAServer;
}

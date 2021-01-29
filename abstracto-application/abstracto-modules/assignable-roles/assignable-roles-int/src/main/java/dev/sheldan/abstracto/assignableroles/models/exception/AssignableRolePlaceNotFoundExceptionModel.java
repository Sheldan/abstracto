package dev.sheldan.abstracto.assignableroles.models.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class AssignableRolePlaceNotFoundExceptionModel implements Serializable {
    private final Long placeId;
}

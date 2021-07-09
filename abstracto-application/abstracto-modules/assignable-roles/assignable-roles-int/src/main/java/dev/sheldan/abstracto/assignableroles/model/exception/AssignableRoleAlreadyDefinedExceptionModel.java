package dev.sheldan.abstracto.assignableroles.model.exception;

import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Contains the model for {@link dev.sheldan.abstracto.assignableroles.exception.AssignableRoleAlreadyDefinedException}
 */
@Getter
@Builder
public class AssignableRoleAlreadyDefinedExceptionModel implements Serializable {
    private final RoleDisplay roleDisplay;
    private final String placeName;
}

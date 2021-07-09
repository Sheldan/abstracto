package dev.sheldan.abstracto.assignableroles.model.exception;

import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Contains the model for {@link dev.sheldan.abstracto.assignableroles.exception.AssignableRolePlaceNotFoundException}
 */
@Getter
@Builder
public class AssignableRoleNotUsableExceptionModel implements Serializable {
    private final RoleDisplay roleDisplay;
}

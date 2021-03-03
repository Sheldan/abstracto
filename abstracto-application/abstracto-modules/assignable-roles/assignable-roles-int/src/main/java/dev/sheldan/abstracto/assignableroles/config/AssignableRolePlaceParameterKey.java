package dev.sheldan.abstracto.assignableroles.config;

import dev.sheldan.abstracto.core.command.execution.CommandParameterKey;
import lombok.Getter;

/**
 * This enum is used to define the different key for which there exist properties on an {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace}.
 * And is used for the command parameter when changing the value of an attribute on this place.
 */
@Getter
public enum AssignableRolePlaceParameterKey implements CommandParameterKey {
    INLINE, UNIQUE, AUTOREMOVE, ACTIVE
}

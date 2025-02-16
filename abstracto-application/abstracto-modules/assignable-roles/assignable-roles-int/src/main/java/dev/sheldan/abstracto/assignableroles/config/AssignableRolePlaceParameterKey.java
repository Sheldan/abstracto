package dev.sheldan.abstracto.assignableroles.config;

import dev.sheldan.abstracto.core.command.execution.CommandParameterKey;
import lombok.Getter;

@Getter
public enum AssignableRolePlaceParameterKey implements CommandParameterKey {
    UNIQUE, TEXT
}

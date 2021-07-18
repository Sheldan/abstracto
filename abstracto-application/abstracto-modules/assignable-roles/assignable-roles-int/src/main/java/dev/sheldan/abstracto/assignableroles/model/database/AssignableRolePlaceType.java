package dev.sheldan.abstracto.assignableroles.model.database;

import dev.sheldan.abstracto.core.command.execution.CommandParameterKey;
import lombok.Getter;

@Getter
public enum AssignableRolePlaceType implements CommandParameterKey {
    DEFAULT, BOOSTER
}

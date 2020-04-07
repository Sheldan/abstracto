package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;

import java.util.List;

public interface ConditionalCommand extends Command {
    List<CommandCondition> getConditions();
}

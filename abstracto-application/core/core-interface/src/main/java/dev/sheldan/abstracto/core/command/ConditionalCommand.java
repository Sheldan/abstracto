package dev.sheldan.abstracto.core.command;

import java.util.List;

public interface ConditionalCommand extends Command {
    List<CommandCondition> getConditions();
}

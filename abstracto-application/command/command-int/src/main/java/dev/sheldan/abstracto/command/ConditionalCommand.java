package dev.sheldan.abstracto.command;

import java.util.List;

public interface ConditionalCommand extends Command {
    List<CommandCondition> getConditions();
}

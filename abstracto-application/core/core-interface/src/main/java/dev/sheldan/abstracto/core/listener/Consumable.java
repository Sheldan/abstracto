package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.execution.result.ExecutionResult;
import net.dv8tion.jda.api.events.Event;

public interface Consumable {
    default boolean shouldConsume(Event event, ExecutionResult result) { return false; }
}

package dev.sheldan.abstracto.core.listener;

import net.dv8tion.jda.api.events.Event;

public interface Consumable {
    default boolean shouldConsume(Event event, ConsumableListenerResult result) { return false; }
}

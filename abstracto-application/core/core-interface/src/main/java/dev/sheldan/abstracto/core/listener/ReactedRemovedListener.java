package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.MessageReaction;

public interface ReactedRemovedListener {
    void executeReactionRemoved(CachedMessage message, MessageReaction reaction, AUserInAServer userRemoving);
}

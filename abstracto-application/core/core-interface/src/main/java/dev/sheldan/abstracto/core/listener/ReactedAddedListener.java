package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.MessageReaction;

public interface ReactedAddedListener {
    void executeReactionAdded(CachedMessage message, MessageReaction reaction, AUserInAServer userAdding);
}

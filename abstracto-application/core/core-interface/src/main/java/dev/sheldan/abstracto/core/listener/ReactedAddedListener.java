package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.MessageReaction;

public interface ReactedAddedListener extends FeatureAware {
    void executeReactionAdded(CachedMessage message, MessageReaction reaction, AUserInAServer userAdding);
}

package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import net.dv8tion.jda.api.entities.MessageReaction;

public interface ReactedAddedListener extends FeatureAware {
    void executeReactionAdded(CachedMessage message, MessageReaction reaction, UserInServerDto userAdding);
}

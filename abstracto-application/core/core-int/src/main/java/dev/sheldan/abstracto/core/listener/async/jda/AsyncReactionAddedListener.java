package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;

public interface AsyncReactionAddedListener extends FeatureAware {
    void executeReactionAdded(CachedMessage message, CachedReactions addedReaction, ServerUser userAdding);
}

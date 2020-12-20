package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;

public interface AsyncReactionRemovedListener extends FeatureAware {
    void executeReactionRemoved(CachedMessage message, CachedReactions removedReaction, ServerUser userRemoving);
}

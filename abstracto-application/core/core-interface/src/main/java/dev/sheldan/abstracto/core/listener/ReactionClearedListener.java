package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;

public interface ReactionClearedListener extends FeatureAware {
    void executeReactionCleared(CachedMessage message);
}

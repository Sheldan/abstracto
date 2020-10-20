package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;

public interface ReactionClearedListener extends FeatureAware, Prioritized {
    void executeReactionCleared(CachedMessage message);
}

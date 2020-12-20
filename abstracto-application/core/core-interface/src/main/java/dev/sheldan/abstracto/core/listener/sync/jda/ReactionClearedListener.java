package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;

public interface ReactionClearedListener extends FeatureAware, Prioritized {
    void executeReactionCleared(CachedMessage message);
}

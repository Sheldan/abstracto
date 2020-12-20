package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;

public interface AsyncEmoteDeletedListener extends FeatureAware, Prioritized {
    void emoteDeleted(CachedEmote deletedEmote);
}

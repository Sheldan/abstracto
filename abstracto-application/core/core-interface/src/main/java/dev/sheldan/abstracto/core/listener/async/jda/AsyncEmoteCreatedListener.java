package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;

public interface AsyncEmoteCreatedListener extends FeatureAware, Prioritized {
    void emoteCreated(CachedEmote createdEmote);
}

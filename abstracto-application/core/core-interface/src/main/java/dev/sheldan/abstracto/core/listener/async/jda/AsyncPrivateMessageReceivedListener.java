package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;

public interface AsyncPrivateMessageReceivedListener extends FeatureAware {
    void execute(CachedMessage message);
}

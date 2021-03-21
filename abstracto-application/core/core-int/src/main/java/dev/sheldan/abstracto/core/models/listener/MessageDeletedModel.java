package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageDeletedModel implements FeatureAwareListenerModel {

    private CachedMessage cachedMessage;

    @Override
    public Long getServerId() {
        return cachedMessage.getServerId();
    }
}

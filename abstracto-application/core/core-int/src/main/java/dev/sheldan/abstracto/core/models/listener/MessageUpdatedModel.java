package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;

@Getter
@Setter
@Builder
public class MessageUpdatedModel implements FeatureAwareListenerModel {

    private CachedMessage before;
    private Message after;

    @Override
    public Long getServerId() {
        return before.getServerId();
    }
}

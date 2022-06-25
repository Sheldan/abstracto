package dev.sheldan.abstracto.moderation.model.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InfractionDescriptionEventModel implements FeatureAwareListenerModel {
    private Long userId;
    private Long serverId;
    private Long infractionId;
    private String newDescription;
    private String type;

    @Override
    public Long getServerId() {
        return serverId;
    }
}

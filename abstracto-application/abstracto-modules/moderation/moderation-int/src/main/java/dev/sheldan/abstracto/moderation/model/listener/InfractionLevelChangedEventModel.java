package dev.sheldan.abstracto.moderation.model.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InfractionLevelChangedEventModel implements FeatureAwareListenerModel {
    private Integer oldLevel;
    private Long oldPoints;
    private Integer newLevel;
    private Long newPoints;
    private Long userId;
    private Long serverId;

    @Override
    public Long getServerId() {
        return serverId;
    }
}

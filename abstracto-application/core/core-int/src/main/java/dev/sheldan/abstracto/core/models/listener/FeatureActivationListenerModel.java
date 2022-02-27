package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.ListenerModel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeatureActivationListenerModel implements ListenerModel {
    private String featureName;
    private Long serverId;
}


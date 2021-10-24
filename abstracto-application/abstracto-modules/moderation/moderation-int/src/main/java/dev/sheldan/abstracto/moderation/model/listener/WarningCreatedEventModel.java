package dev.sheldan.abstracto.moderation.model.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WarningCreatedEventModel implements FeatureAwareListenerModel {
    private Long warningId;
    private Long warnedUserId;
    private Long serverId;
    private Long warningUserId;
    private Long warningChannelId;
    private Long warningMessageId;
    private String reason;

    @Override
    public Long getServerId() {
        return serverId;
    }
}

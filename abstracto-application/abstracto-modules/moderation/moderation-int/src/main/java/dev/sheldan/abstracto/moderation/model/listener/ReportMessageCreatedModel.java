package dev.sheldan.abstracto.moderation.model.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerUser;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReportMessageCreatedModel implements FeatureAwareListenerModel {
    private ServerChannelMessage reportMessage;
    private ServerChannelMessage reportedMessage;
    private ServerUser reporter;

    @Override
    public Long getServerId() {
        return reportMessage.getServerId();
    }
}

package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.ListenerModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChannelGroupCreatedListenerModel implements ListenerModel {
    private Long channelGroupId;
}

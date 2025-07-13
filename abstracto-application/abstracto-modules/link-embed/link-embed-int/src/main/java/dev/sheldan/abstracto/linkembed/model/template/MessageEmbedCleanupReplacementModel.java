package dev.sheldan.abstracto.linkembed.model.template;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MessageEmbedCleanupReplacementModel {
    private ServerChannelMessage message;
}

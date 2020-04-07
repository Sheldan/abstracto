package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AServerAChannelMessage {
    private AServer server;
    private AChannel channel;
    private Long messageId;
}

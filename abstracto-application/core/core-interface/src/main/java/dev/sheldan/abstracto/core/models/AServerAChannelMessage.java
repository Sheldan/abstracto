package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AServerAChannelMessage {
    private ServerDto server;
    private ChannelDto channel;
    private Long messageId;
}

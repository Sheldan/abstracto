package dev.sheldan.abstracto.core.command.model.exception;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class ChannelAlreadyInChannelGroupExceptionModel implements Serializable {
    private AChannel channel;
    private AChannelGroup channelGroup;
}

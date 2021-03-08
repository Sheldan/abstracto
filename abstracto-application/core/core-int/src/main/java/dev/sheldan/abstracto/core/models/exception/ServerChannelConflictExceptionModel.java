package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class ServerChannelConflictExceptionModel implements Serializable {
    private final Long serverId;
    private final Long channelId;
}

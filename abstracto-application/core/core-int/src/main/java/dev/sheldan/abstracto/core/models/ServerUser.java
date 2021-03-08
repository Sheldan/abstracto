package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class ServerUser {
    private Long serverId;
    private Long userId;
}

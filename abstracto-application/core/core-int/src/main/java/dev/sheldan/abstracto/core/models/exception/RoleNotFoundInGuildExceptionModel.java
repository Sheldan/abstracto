package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class RoleNotFoundInGuildExceptionModel implements Serializable {
    private final Long roleId;
    private final Long serverId;
}

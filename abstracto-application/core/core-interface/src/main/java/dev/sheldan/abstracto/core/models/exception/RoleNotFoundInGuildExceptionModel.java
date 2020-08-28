package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Builder
public class RoleNotFoundInGuildExceptionModel implements Serializable {
    private final Long roleId;
    private final Long serverId;
}

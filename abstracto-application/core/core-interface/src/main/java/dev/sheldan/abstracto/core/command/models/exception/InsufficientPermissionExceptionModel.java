package dev.sheldan.abstracto.core.command.models.exception;

import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class InsufficientPermissionExceptionModel implements Serializable {
    private transient List<Role> allowedRoles;
}

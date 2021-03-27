package dev.sheldan.abstracto.core.command.model.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

@Setter
@Getter
@Builder
public class InsufficientPermissionMessage {
    private List<Role> allowedRoles;
}

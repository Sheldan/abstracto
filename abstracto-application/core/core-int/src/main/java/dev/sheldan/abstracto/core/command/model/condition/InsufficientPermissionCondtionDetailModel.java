package dev.sheldan.abstracto.core.command.model.condition;

import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class InsufficientPermissionCondtionDetailModel implements Serializable {
    private transient List<Role> allowedRoles;
}

package dev.sheldan.abstracto.assignableroles.models.exception;

import dev.sheldan.abstracto.core.models.FullRole;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

import java.io.Serializable;

@Getter
@Builder
public class AssignableRoleNotUsableExceptionModel implements Serializable {
    private final FullRole role;
    private final transient Guild guild;
}

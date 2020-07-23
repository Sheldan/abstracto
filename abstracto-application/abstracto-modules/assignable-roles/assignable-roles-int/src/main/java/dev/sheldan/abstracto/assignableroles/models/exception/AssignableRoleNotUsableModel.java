package dev.sheldan.abstracto.assignableroles.models.exception;

import dev.sheldan.abstracto.core.models.FullRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class AssignableRoleNotUsableModel implements Serializable {
    private FullRole role;
    private Guild guild;
}

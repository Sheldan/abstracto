package dev.sheldan.abstracto.assignableroles.models.exception;

import dev.sheldan.abstracto.core.models.FullRole;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

import java.io.Serializable;

/**
 * Contains the model for {@link dev.sheldan.abstracto.assignableroles.exceptions.AssignableRolePlaceNotFoundException}
 */
@Getter
@Builder
public class AssignableRoleNotUsableExceptionModel implements Serializable {
    /**
     * The {@link FullRole role} which is not usable as an {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRole role}
     */
    private final FullRole role;
    /**
     * The {@link Guild server} in which it was not possible to use the {@link net.dv8tion.jda.api.entities.Role role}
     */
    private final transient Guild guild;
}

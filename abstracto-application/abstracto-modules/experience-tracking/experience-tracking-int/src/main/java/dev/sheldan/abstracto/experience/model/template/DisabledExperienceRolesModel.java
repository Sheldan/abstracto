package dev.sheldan.abstracto.experience.model.template;

import dev.sheldan.abstracto.core.models.FullRole;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Model used to render  an overview of the roles for which experience gain has been disabled on the current server.
 */
@Getter
@SuperBuilder
public class DisabledExperienceRolesModel extends UserInitiatedServerContext {
    /**
     * A list of {@link FullRole roles} for which experience gain is disabled in this server
     */
    @Builder.Default
    private List<FullRole> roles = new ArrayList<>();
}

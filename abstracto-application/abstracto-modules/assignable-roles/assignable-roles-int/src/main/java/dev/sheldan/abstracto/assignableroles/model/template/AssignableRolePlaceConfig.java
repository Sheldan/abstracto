package dev.sheldan.abstracto.assignableroles.model.template;

import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * This model is used as a container to display the configuration of an {@link AssignableRolePlace place}
 */
@Getter
@Setter
@Builder
public class AssignableRolePlaceConfig {
    /**
     * The {@link AssignableRolePlace place} to show the config of
     */
    private AssignableRolePlace place;
    /**
     * The {@link AssignableRolePlaceConfig roles} which are contained in this {@link AssignableRolePlace}
     */
    private List<AssignableRolePlaceConfigRole> roles;
}

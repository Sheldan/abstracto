package dev.sheldan.abstracto.assignableroles.models.templates;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Model used to render the {@link AssignableRolePlace place}
 */
@Getter
@Setter
@Builder
public class AssignablePostMessage {
    /**
     * The {@link AssignableRolePlace place} to render
     */
    private AssignableRolePlace place;
    /**
     * The awarded {@link AssignablePostRole roles} for this {@link AssignableRolePlace place}
     */
    private List<AssignablePostRole> roles;
    /**
     * The highest number of the position within the {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRole} of the
     * {@link AssignableRolePlace place}
     */
    private Integer maxPosition;
}

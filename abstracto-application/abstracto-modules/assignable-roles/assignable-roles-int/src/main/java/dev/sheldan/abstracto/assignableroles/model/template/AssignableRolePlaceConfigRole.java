package dev.sheldan.abstracto.assignableroles.model.template;


import dev.sheldan.abstracto.assignableroles.model.template.condition.AssignableRoleConditionDisplay;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Model used to display the configuration of an individual {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRole}
 * within an {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace place}
 */
@Getter
@Setter
@Builder
public class AssignableRolePlaceConfigRole {
    private String description;
    private String emoteMarkDown;
    private RoleDisplay roleDisplay;
    private List<AssignableRoleConditionDisplay> conditions;
}

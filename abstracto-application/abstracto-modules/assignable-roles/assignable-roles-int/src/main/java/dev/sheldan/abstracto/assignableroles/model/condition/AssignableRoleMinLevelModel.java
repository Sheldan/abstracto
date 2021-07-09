package dev.sheldan.abstracto.assignableroles.model.condition;

import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AssignableRoleMinLevelModel {
    private Integer minLevel;
    private RoleDisplay roleDisplay;
}

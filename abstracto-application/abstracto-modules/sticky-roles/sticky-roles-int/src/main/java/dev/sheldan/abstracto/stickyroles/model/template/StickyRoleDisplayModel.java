package dev.sheldan.abstracto.stickyroles.model.template;

import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StickyRoleDisplayModel {
    private RoleDisplay roleDisplay;
    private Boolean sticky;
}

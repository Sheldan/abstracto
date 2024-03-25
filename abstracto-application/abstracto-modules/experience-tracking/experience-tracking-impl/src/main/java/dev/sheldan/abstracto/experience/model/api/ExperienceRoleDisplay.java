package dev.sheldan.abstracto.experience.model.api;

import dev.sheldan.abstracto.core.models.frontend.RoleDisplay;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExperienceRoleDisplay {
    private RoleDisplay role;
    private Integer level;
}

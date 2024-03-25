package dev.sheldan.abstracto.experience.model.api;

import dev.sheldan.abstracto.core.models.frontend.RoleDisplay;
import dev.sheldan.abstracto.core.models.frontend.UserDisplay;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class UserExperienceDisplay {
    private UserDisplay member;
    private Long id;
    private Integer level;
    private Long experience;
    private Long messages;
    private RoleDisplay role;
}

package dev.sheldan.abstracto.experience.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LevelUpNotificationModel {
    private MemberDisplay memberDisplay;
    private Integer oldLevel;
    private Integer newLevel;
    private RoleDisplay oldRole;
    private RoleDisplay newRole;
    private Long oldExperience;
    private Long newExperience;
}

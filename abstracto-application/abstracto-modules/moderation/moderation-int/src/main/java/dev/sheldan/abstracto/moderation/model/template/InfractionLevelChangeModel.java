package dev.sheldan.abstracto.moderation.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InfractionLevelChangeModel {
    private Long newPoints;
    private Long oldPoints;
    private Integer newLevel;
    private Integer oldLevel;
    private MemberDisplay member;
}

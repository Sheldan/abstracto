package dev.sheldan.abstracto.experience.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LevelActionDisplay {
    private String actionKey;
    private Integer level;
    private MemberDisplay member;
    private String parameters;
}

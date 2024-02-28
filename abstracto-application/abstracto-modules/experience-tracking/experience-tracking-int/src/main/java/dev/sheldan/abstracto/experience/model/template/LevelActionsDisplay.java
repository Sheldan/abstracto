package dev.sheldan.abstracto.experience.model.template;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LevelActionsDisplay {
    private List<LevelActionDisplay> actions;
}

package dev.sheldan.abstracto.experience.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class LevelRolesModel {
    private List<LevelRole> levelRoles;
}

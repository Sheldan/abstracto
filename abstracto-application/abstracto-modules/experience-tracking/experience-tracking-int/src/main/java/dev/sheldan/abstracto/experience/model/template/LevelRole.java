package dev.sheldan.abstracto.experience.model.template;

import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;

@Getter
@Builder
public class LevelRole {
    private Role role;
    private Long roleId;
    private Integer level;
}

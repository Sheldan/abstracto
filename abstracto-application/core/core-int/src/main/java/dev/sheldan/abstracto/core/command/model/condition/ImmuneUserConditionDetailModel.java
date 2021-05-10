package dev.sheldan.abstracto.core.command.model.condition;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;

@Getter
@Setter
@Builder
public class ImmuneUserConditionDetailModel {
    private Role role;
    private String effectTypeKey;
}

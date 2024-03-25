package dev.sheldan.abstracto.core.models.frontend;

import dev.sheldan.abstracto.core.models.database.ARole;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;

@Getter
@Builder
public class RoleDisplay {
    private Long id;
    private String name;
    private Integer r;
    private Integer g;
    private Integer b;

    public static RoleDisplay fromRole(Role role) {
        RoleDisplayBuilder builder = builder()
                .name(role.getName());
        Color roleColor = role.getColor();
        if(roleColor != null) {
            builder.r(roleColor.getRed()).
                    b(roleColor.getBlue())
                    .g(roleColor.getGreen());
        }
        return builder.build();
    }
    public static RoleDisplay fromARole(ARole role) {
        return builder()
                .id(role.getId())
                .build();
    }
}

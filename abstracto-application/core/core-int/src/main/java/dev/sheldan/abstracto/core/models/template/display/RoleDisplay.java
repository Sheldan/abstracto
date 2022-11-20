package dev.sheldan.abstracto.core.models.template.display;

import dev.sheldan.abstracto.core.models.database.ARole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;

@Getter
@Setter
@Builder
public class RoleDisplay {
    private String roleMention;
    private Long roleId;

    public static RoleDisplay fromRole(Role role) {
        return RoleDisplay
                .builder()
                .roleId(role.getIdLong())
                .roleMention(role.getAsMention())
                .build();
    }

    public static RoleDisplay fromRole(Long roleId) {
        return RoleDisplay
                .builder()
                .roleId(roleId)
                .roleMention("<@&" + roleId + '>')
                .build();
    }

    public static RoleDisplay fromARole(ARole role) {
        return RoleDisplay
                .builder()
                .roleId(role.getId())
                .roleMention(role.getAsMention())
                .build();
    }
}

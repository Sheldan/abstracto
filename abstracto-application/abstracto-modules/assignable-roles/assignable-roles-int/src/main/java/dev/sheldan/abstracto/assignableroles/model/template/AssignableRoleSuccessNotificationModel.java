package dev.sheldan.abstracto.assignableroles.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

@Getter
@Setter
@Builder
public class AssignableRoleSuccessNotificationModel {
    private Role role;
    private Boolean added;
    private List<Role> removedRoles;
}

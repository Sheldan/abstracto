package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlaceType;
import dev.sheldan.abstracto.assignableroles.model.database.AssignedRoleUser;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public interface AssignableRoleManagementService {
    AssignableRole addRoleToPlace(Emoji emoji, Role role, String description, AssignableRolePlace place, ComponentPayload componentPayload);

    AssignableRole getByAssignableRoleId(Long assignableRoleId);
    void deleteAssignableRole(AssignableRole assignableRole);
    List<AssignableRole> getAssignableRolesFromAssignableUserWithPlaceType(AssignedRoleUser user, AssignableRolePlaceType type);

}

package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlaceType;
import dev.sheldan.abstracto.assignableroles.model.database.AssignedRoleUser;
import dev.sheldan.abstracto.assignableroles.repository.AssignableRoleRepository;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AssignableRoleManagementServiceBean implements AssignableRoleManagementService {

    @Autowired
    private AssignableRolePlaceManagementService rolePlaceManagementService;

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private AssignableRoleRepository repository;

    @Autowired
    private EmoteService emoteService;

    @Override
    public AssignableRole addRoleToPlace(FullEmote emote, Role role, String description, AssignableRolePlace place, ComponentPayload componentPayload) {
        ARole arole = roleManagementService.findRole(role.getIdLong());
        AssignableRole roleToAdd = AssignableRole
                .builder()
                .assignablePlace(place)
                .emoteMarkdown(emote != null ? emote.getEmoteRepr() : null)
                .role(arole)
                .componentPayload(componentPayload)
                .server(place.getServer())
                .description(description)
                .build();
        place.getAssignableRoles().add(roleToAdd);
        log.info("Adding role {} to assignable role place {}. There are now {} roles.", role.getId(), place.getId(), place.getAssignableRoles().size());
        return roleToAdd;
    }

    @Override
    public AssignableRole getByAssignableRoleId(Long assignableRoleId) {
        return repository.findById(assignableRoleId).orElseThrow(() -> new AbstractoRunTimeException("Assignable role not found"));
    }

    @Override
    public void deleteAssignableRole(AssignableRole assignableRole) {
        assignableRole.getAssignablePlace().getAssignableRoles().remove(assignableRole);
        assignableRole.setAssignablePlace(null);
        repository.delete(assignableRole);
    }

    @Override
    public List<AssignableRole> getAssignableRolesFromAssignableUserWithPlaceType(AssignedRoleUser user, AssignableRolePlaceType type) {
        return repository.findByAssignedUsersContainingAndAssignablePlace_Type(user, type);
    }

}

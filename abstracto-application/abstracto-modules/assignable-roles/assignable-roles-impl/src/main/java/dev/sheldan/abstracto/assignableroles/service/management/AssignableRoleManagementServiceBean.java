package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.exception.AssignableRolePlaceNotFoundException;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.assignableroles.repository.AssignableRoleRepository;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private AssignableRolePlacePostManagementService postManagementService;

    @Autowired
    private AssignableRoleRepository repository;

    @Autowired
    private EmoteService emoteService;

    @Override
    public AssignableRole addRoleToPlace(AssignableRolePlace place, AEmote emote, ARole role, String description, AssignableRolePlacePost post) {
        Integer maxPosition = place.getAssignableRoles().stream().map(AssignableRole::getPosition).max(Integer::compareTo).orElse(0);
        if(!place.getAssignableRoles().isEmpty()) {
            maxPosition += 1;
        }
        AssignableRole roleToAdd = AssignableRole
                .builder()
                .assignablePlace(place)
                .emote(emote)
                .role(role)
                .requiredLevel(0)
                .server(place.getServer())
                .position(maxPosition)
                .description(description)
                .assignableRolePlacePost(post)
                .build();
        place.getAssignableRoles().add(roleToAdd);
        log.info("Adding role {} to assignable role place {}. There are now {} roles.", role.getId(), place.getId(), place.getAssignableRoles().size());
        return roleToAdd;
    }

    @Override
    public AssignableRole addRoleToPlace(Long placeId, Integer emoteId, Long roleId, String description, Long messageId) {
        AssignableRolePlace place = rolePlaceManagementService.findByPlaceIdOptional(placeId).orElseThrow(() -> new AssignableRolePlaceNotFoundException(placeId));
        AEmote emote = emoteManagementService.loadEmote(emoteId);
        ARole role = roleManagementService.findRole(roleId);
        AssignableRolePlacePost post = postManagementService.findByMessageId(messageId);
        AssignableRole assignableRole = addRoleToPlace(place, emote, role, description, post);
        post.getAssignableRoles().add(assignableRole);
        return assignableRole;
    }

    @Override
    public AssignableRole addRoleToPlace(Long placeId, Integer emoteId, Long roleId, String description) {
        AssignableRolePlace place = rolePlaceManagementService.findByPlaceIdOptional(placeId).orElseThrow(() -> new AssignableRolePlaceNotFoundException(placeId));
        AEmote emote = emoteManagementService.loadEmote(emoteId);
        ARole role = roleManagementService.findRole(roleId);
        return addRoleToPlace(place, emote, role, description, null);
    }

    @Override
    public AssignableRole getByAssignableRoleId(Long assignableRoleId) {
        return repository.findById(assignableRoleId).orElseThrow(() -> new AbstractoRunTimeException("Assignable role not found"));
    }

    @Override
    public AssignableRole getRoleForReactionEmote(CachedEmote cachedEmote, AssignableRolePlace assignableRolePlace) {
        for (AssignableRolePlacePost post : assignableRolePlace.getMessagePosts()) {
            for (AssignableRole assignableRole : post.getAssignableRoles()) {
                if (emoteService.compareCachedEmoteWithAEmote(cachedEmote, assignableRole.getEmote())) {
                    return assignableRole;
                }
            }
        }
        throw new AbstractoRunTimeException("Role for reaction was not found.");
    }
}

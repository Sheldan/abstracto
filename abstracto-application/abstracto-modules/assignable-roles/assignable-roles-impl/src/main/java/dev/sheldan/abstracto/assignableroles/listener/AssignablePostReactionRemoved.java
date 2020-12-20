package dev.sheldan.abstracto.assignableroles.listener;

import dev.sheldan.abstracto.assignableroles.config.features.AssignableRoleFeature;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.assignableroles.service.AssignableRoleService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlacePostManagementService;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionRemovedListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class AssignablePostReactionRemoved implements AsyncReactionRemovedListener {

    @Autowired
    private AssignableRolePlacePostManagementService service;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private AssignableRoleService assignableRoleService;

    @Override
    public FeatureEnum getFeature() {
        return AssignableRoleFeature.ASSIGNABLE_ROLES;
    }

    @Override
    public void executeReactionRemoved(CachedMessage message, CachedReactions reactions, ServerUser userRemoving) {
        Optional<AssignableRolePlacePost> messageOptional = service.findByMessageIdOptional(message.getMessageId());
        if(messageOptional.isPresent()) {
            AssignableRolePlacePost assignablePlacePost = messageOptional.get();
            if(assignablePlacePost.getAssignablePlace().getActive()) {
                assignablePlacePost.getAssignableRoles().forEach(assignableRole -> {
                    if(emoteService.compareCachedEmoteWithAEmote(reactions.getEmote(), assignableRole.getEmote())) {
                        Long assignableRoleId = assignableRole.getId();
                        log.info("Removing assignable role {} for user {} in server {} from assignable role place {}.", assignableRoleId,
                                userRemoving.getUserId(), userRemoving.getServerId(), assignablePlacePost.getAssignablePlace().getId());
                        assignableRoleService.fullyRemoveAssignableRoleFromUser(assignableRole, userRemoving).exceptionally(throwable -> {
                            log.error("Failed to remove assignable role {} from user {} in server {}.", assignableRoleId, userRemoving.getUserId(), userRemoving.getServerId(), throwable);
                            return null;
                        });
                    }
                });
            } else {
                log.trace("Reaction for assignable place {} in sever {} was added, but place is inactive.", assignablePlacePost.getAssignablePlace().getKey(), userRemoving.getServerId());
            }
        }
    }

}

package dev.sheldan.abstracto.assignableroles.listener;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.assignableroles.service.AssignableRoleService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlacePostManagementService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionRemovedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.ReactionRemovedModel;
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
    public FeatureDefinition getFeature() {
        return AssignableRoleFeatureDefinition.ASSIGNABLE_ROLES;
    }

    /**
     * Determines if the {@link net.dv8tion.jda.api.entities.Message message} a reaction was removed from, belongs to a
     * {@link AssignableRolePlacePost post}.
     * If the {@link AssignableRolePlacePost post} belong to an inactive {@link AssignableRolePlace place} this method ignores the removal.
     * Otherwise the logic according to the configuration
     * of the {@link AssignableRolePlace place} will be executed.
     * @param model The {@link ReactionRemovedModel model} containing the information which reaction was placed where
     */
    @Override
    public DefaultListenerResult execute(ReactionRemovedModel model) {
        CachedMessage message = model.getMessage();

        Optional<AssignableRolePlacePost> messageOptional = service.findByMessageIdOptional(message.getMessageId());
        if(messageOptional.isPresent()) {
            AssignableRolePlacePost assignablePlacePost = messageOptional.get();
            if(assignablePlacePost.getAssignablePlace().getActive()) {
                assignablePlacePost.getAssignableRoles().forEach(assignableRole -> {
                    if(emoteService.isReactionEmoteAEmote(model.getReaction().getReactionEmote(), assignableRole.getEmote())) {
                        Long assignableRoleId = assignableRole.getId();
                        log.info("Removing assignable role {} for user {} in server {} from assignable role place {}.", assignableRoleId,
                                model.getUserRemoving().getUserId(), model.getServerId(), assignablePlacePost.getAssignablePlace().getId());
                        assignableRoleService.fullyRemoveAssignableRoleFromUser(assignableRole, model.getUserRemoving()).exceptionally(throwable -> {
                            log.error("Failed to remove assignable role {} from user {} in server {}.", assignableRoleId, model.getUserRemoving().getUserId(), model.getServerId(), throwable);
                            return null;
                        });
                    }
                });
                return DefaultListenerResult.PROCESSED;
            } else {
                log.debug("Reaction for assignable place {} in sever {} was added, but place is inactive.", assignablePlacePost.getAssignablePlace().getKey(), model.getServerId());
                return DefaultListenerResult.PROCESSED;
            }
        } else {
            return DefaultListenerResult.IGNORED;
        }
    }



}

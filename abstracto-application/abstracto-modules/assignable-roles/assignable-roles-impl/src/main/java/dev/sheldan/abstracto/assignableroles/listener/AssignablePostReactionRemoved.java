package dev.sheldan.abstracto.assignableroles.listener;

import dev.sheldan.abstracto.assignableroles.config.features.AssignableRoleFeature;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.assignableroles.service.AssignableRoleService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlacePostManagementService;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.ReactedRemovedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class AssignablePostReactionRemoved implements ReactedRemovedListener {

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
    public void executeReactionRemoved(CachedMessage message, GuildMessageReactionRemoveEvent event, AUserInAServer userRemoving) {
        Optional<AssignableRolePlacePost> messageOptional = service.findByMessageIdOptional(message.getMessageId());
        if(messageOptional.isPresent()) {
            MessageReaction.ReactionEmote reactionEmote = event.getReactionEmote();
            AssignableRolePlacePost assignablePlacePost = messageOptional.get();
            if(assignablePlacePost.getAssignablePlace().getActive()) {
                assignablePlacePost.getAssignableRoles().forEach(assignableRole -> {
                    if(emoteService.isReactionEmoteAEmote(reactionEmote, assignableRole.getEmote())) {
                        Long assignableRoleId = assignableRole.getId();
                        log.info("Removing assignable role {} for user {} in server {} from assignable role place {}.", assignableRoleId,
                                userRemoving.getUserReference().getId(), userRemoving.getServerReference().getId(), assignablePlacePost.getAssignablePlace().getId());
                        assignableRoleService.fullyRemoveAssignableRoleFromUser(assignableRole, event.getMember()).exceptionally(throwable -> {
                            log.error("Failed to remove assignable role {} from user {}.", assignableRoleId, event.getMember(), throwable);
                            return null;
                        });
                    }
                });
            } else {
                log.trace("Reaction for assignable place {} in sever {} was added, but place is inactive.", assignablePlacePost.getAssignablePlace().getKey(), userRemoving.getServerReference().getId());
            }
        }
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.HIGH;
    }
}

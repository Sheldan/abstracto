package dev.sheldan.abstracto.assignableroles.listener;

import dev.sheldan.abstracto.assignableroles.config.features.AssignableRoleFeature;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser;
import dev.sheldan.abstracto.assignableroles.service.AssignableRolePlaceService;
import dev.sheldan.abstracto.assignableroles.service.AssignableRoleServiceBean;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlacePostManagementService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignedRoleUserManagementService;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.ReactedAddedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.EmoteService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AssignablePostReactionAdded implements ReactedAddedListener {

    @Autowired
    private AssignableRolePlacePostManagementService service;

    @Autowired
    private AssignableRoleServiceBean assignableRoleServiceBean;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private AssignableRolePlaceService assignableRolePlaceService;

    @Autowired
    private AssignedRoleUserManagementService assignedRoleUserManagementService;

    @Override
    public void executeReactionAdded(CachedMessage message, GuildMessageReactionAddEvent event, AUserInAServer userAdding) {
        Optional<AssignableRolePlacePost> messageOptional = service.findByMessageIdOptional(message.getMessageId());
        if(messageOptional.isPresent()) {
            MessageReaction reaction = event.getReaction();
            AssignableRolePlacePost assignablePlacePost = messageOptional.get();
            if(reaction.isSelf()) {
                log.info("Ignoring self reaction on assignable role post in server {}.", message.getServerId());
                return;
            }
            MessageReaction.ReactionEmote reactionEmote = event.getReactionEmote();
            if(assignablePlacePost.getAssignablePlace().getActive()) {
                addAppropriateRoles(event, reaction, assignablePlacePost, reactionEmote, userAdding);
            } else {
                reaction.removeReaction(event.getUser()).submit();
                log.trace("Reaction for assignable place {} in sever {} was added, but place is inactive.", assignablePlacePost.getAssignablePlace().getKey(), userAdding.getServerReference().getId());
            }
        }
    }

    private void addAppropriateRoles(GuildMessageReactionAddEvent event, MessageReaction reaction, AssignableRolePlacePost assignablePlacePost, MessageReaction.ReactionEmote reactionEmote, AUserInAServer userAdding) {
        boolean validReaction = false;
        AssignableRolePlace assignableRolePlace = assignablePlacePost.getAssignablePlace();
        for (AssignableRole assignableRole : assignablePlacePost.getAssignableRoles()) {
            if (emoteService.isReactionEmoteAEmote(reactionEmote, assignableRole.getEmote())) {
                CompletableFuture<Void> future;
                if(assignableRolePlace.getUniqueRoles()) {
                    Optional<AssignedRoleUser> byUserInServer = assignedRoleUserManagementService.findByUserInServerOptional(userAdding);
                    if(byUserInServer.isPresent()){
                        future = assignableRolePlaceService.removeExistingReactionsAndRoles(assignableRolePlace, byUserInServer.get());
                    } else {
                        future = CompletableFuture.completedFuture(null);
                    }
                } else {
                    future = CompletableFuture.completedFuture(null);
                }

                Long assignableRoleId = assignableRole.getId();
                future.whenComplete((aVoid, throwable) -> {
                    if(throwable != null) {
                        log.warn("Failed to remove previous role assignments for {} in server {} at place {}.",
                                userAdding.getUserReference().getId(), userAdding.getServerReference().getId(), assignablePlacePost.getAssignablePlace().getKey());
                    }
                    assignableRoleServiceBean.assignAssignableRoleToUser(assignableRoleId, event.getMember()).exceptionally(innerThrowable -> {
                        log.error("Failed to add new role assignment.", innerThrowable);
                        return null;
                    });
                });
                validReaction = true;
                break;
            }
        }
        if(!validReaction || assignableRolePlace.getAutoRemove()) {
            reaction.removeReaction(event.getUser()).submit();
        }
    }

    @Override
    public FeatureEnum getFeature() {
        return AssignableRoleFeature.ASSIGNABLE_ROLES;
    }
}

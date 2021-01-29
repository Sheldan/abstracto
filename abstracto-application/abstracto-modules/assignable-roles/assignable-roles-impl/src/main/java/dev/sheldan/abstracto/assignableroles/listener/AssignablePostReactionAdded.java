package dev.sheldan.abstracto.assignableroles.listener;

import dev.sheldan.abstracto.assignableroles.config.features.AssignableRoleFeature;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser;
import dev.sheldan.abstracto.assignableroles.service.AssignableRolePlaceService;
import dev.sheldan.abstracto.assignableroles.service.AssignableRoleServiceBean;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRoleManagementService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlaceManagementService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlacePostManagementService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignedRoleUserManagementService;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionAddedListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.ReactionService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AssignablePostReactionAdded implements AsyncReactionAddedListener {

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

    @Autowired
    private AssignableRolePlaceManagementService assignableRolePlaceManagementService;

    @Autowired
    private AssignablePostReactionAdded self;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private AssignableRoleManagementService assignableRoleManagementService;

    @Autowired
    private ReactionService reactionService;

    @Override
    public void executeReactionAdded(CachedMessage message, CachedReactions cachedReaction, ServerUser serverUser) {
        Optional<AssignableRolePlacePost> messageOptional = service.findByMessageIdOptional(message.getMessageId());
        if(messageOptional.isPresent()) {
            AssignableRolePlacePost assignablePlacePost = messageOptional.get();
            if(cachedReaction.getSelf()) {
                log.info("Ignoring self reaction on assignable role post in server {}.", message.getServerId());
                return;
            }
            CachedReaction specificReaction = cachedReaction.getReactionForSpecificUser(serverUser);
            Long assignableRolePlaceId = assignablePlacePost.getId();
            if(assignablePlacePost.getAssignablePlace().getActive()) {
                log.info("User {} added reaction to assignable role place {} in server {}. Handling added event.", serverUser.getUserId(), assignablePlacePost.getId(), serverUser.getServerId());
                addAppropriateRoles(specificReaction, assignablePlacePost, serverUser, message);
            } else {
                reactionService.removeReactionFromMessage(specificReaction, message).exceptionally(throwable -> {
                    log.error("Failed to remove reaction on place {} because place is inactive.", assignableRolePlaceId, throwable);
                    return null;
                });
                log.trace("Reaction for assignable place {} in sever {} was added, but place is inactive.", assignablePlacePost.getAssignablePlace().getKey(), serverUser.getServerId());
            }
        }
    }

    private void addAppropriateRoles(CachedReaction cachedReaction, AssignableRolePlacePost assignablePlacePost, ServerUser serverUser, CachedMessage message) {
        boolean validReaction = false;
        AssignableRolePlace assignableRolePlace = assignablePlacePost.getAssignablePlace();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (AssignableRole assignableRole : assignablePlacePost.getAssignableRoles()) {
            log.trace("Checking emote {} if it was reaction for assignable role place.", assignableRole.getEmote().getId());
            if (emoteService.compareCachedEmoteWithAEmote(cachedReaction.getEmote(), assignableRole.getEmote())) {
                if(assignableRolePlace.getUniqueRoles()) {
                    log.trace("Assignable role place {} has unique roles configured. Removing existing reactions and roles.", assignableRolePlace.getId());
                    Optional<AssignedRoleUser> byUserInServer = assignedRoleUserManagementService.findByUserInServerOptional(serverUser);
                    byUserInServer.ifPresent(user -> futures.add(assignableRolePlaceService.removeExistingReactionsAndRoles(assignableRolePlace, user)));
                }

                Long assignableRoleId = assignableRole.getId();
                log.info("User added {} reaction {} and gets assignable role {} in server {}.", serverUser.getUserId(), assignableRole.getEmote().getId(), assignableRoleId, serverUser.getServerId());
                CompletableFuture<Void> roleAdditionFuture = assignableRoleServiceBean.assignAssignableRoleToUser(assignableRoleId, serverUser);

                futures.add(CompletableFuture.allOf(roleAdditionFuture));
                validReaction = true;
                break;
            }
        }
        if(!validReaction) {
            log.trace("Reaction was not found in the configuration of assignable role place {}, removing reaction.", assignableRolePlace.getId());
            futures.add(reactionService.removeReactionFromMessage(cachedReaction, message));
        }
        Long assignableRolePlaceId = assignableRolePlace.getId();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(aVoid ->
            self.updateStoredAssignableRoles(assignableRolePlaceId, serverUser, cachedReaction)
        );
    }

    @Transactional
    public void updateStoredAssignableRoles(Long assignableRolePlaceId, ServerUser serverUser, CachedReaction cachedReaction) {
        AssignableRolePlace place = assignableRolePlaceManagementService.findByPlaceId(assignableRolePlaceId);
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(serverUser);
        if(place.getUniqueRoles()) {
            log.trace("Assignable role place {} has unique roles. Deleting all existing references.", assignableRolePlaceId);
            assignableRoleServiceBean.clearAllRolesOfUserInPlace(place, userInAServer);
        }
        AssignableRole role = assignableRoleManagementService.getRoleForReactionEmote(cachedReaction.getEmote(), place);
        log.info("Adding role to assignable role {} to user {} in server {}.", role.getId(), userInAServer.getUserReference().getId(), userInAServer.getServerReference().getId());
        assignableRoleServiceBean.addRoleToUser(role.getId(), userInAServer);

    }

    @Override
    public FeatureEnum getFeature() {
        return AssignableRoleFeature.ASSIGNABLE_ROLES;
    }

}

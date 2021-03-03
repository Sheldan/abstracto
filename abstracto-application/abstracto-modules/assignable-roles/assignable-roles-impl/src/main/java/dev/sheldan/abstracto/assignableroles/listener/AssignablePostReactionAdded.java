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
import dev.sheldan.abstracto.core.service.MemberService;
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

    @Autowired
    private MemberService memberService;

    /**
     * Determines if the {@link net.dv8tion.jda.api.entities.Message message} a reaction was added to, belongs to a
     * {@link AssignableRolePlacePost post}.
     * If the {@link AssignableRolePlacePost post} belong to an inactive {@link AssignableRolePlace place} this method
     * will automatically remove the reaction, self reactions are ignored. Otherwise the logic according to the configuration
     * of the {@link AssignableRolePlace place} will be executed.
     * @param message The {@link CachedMessage message} on which a reaction was added
     * @param cachedReaction The {@link CachedReaction reaction} which was added
     * @param serverUser The {@link ServerUser serverUser} who added the reaction
     */
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
            Long assignableRolePlacePostId = assignablePlacePost.getId();
            if(assignablePlacePost.getAssignablePlace().getActive()) {
                log.info("User {} added reaction to assignable role place {} in server {}. Handling added event.", serverUser.getUserId(), assignablePlacePost.getId(), serverUser.getServerId());
                addAppropriateRoles(specificReaction, assignablePlacePost, serverUser, message);
            } else {
                reactionService.removeReactionFromMessage(specificReaction, message).exceptionally(throwable -> {
                    log.error("Failed to remove reaction on place post {} because place is inactive.", assignableRolePlacePostId, throwable);
                    return null;
                });
                log.trace("Reaction for assignable place {} in sever {} was added, but place is inactive.", assignablePlacePost.getAssignablePlace().getKey(), serverUser.getServerId());
            }
        }
    }

    /**
     * Iterates over all {@link AssignableRole assignableRoles} of the post and checks which {@link AssignableRole assignableRole}
     * is identified by the added {@link CachedReaction reaction}. If there is no valid reaction, the {@link net.dv8tion.jda.api.entities.MessageReaction reaction}
     * will be removed again. In case the {@link AssignableRolePlace place} is configured to have unique roles, this will remove the existing
     * {@link net.dv8tion.jda.api.entities.MessageReaction reaction} and the assigned {@link net.dv8tion.jda.api.entities.Role role}.
     * Afterwards the appropriate {@link net.dv8tion.jda.api.entities.Role role} will be added and the update
     * will be stored in the database.
     * @param cachedReaction The {@link CachedReaction reaction} which was added
     * @param assignablePlacePost The {@link AssignableRolePlacePost post} onto which the {@link CachedReaction reaction} was added to
     * @param serverUser The {@link ServerUser serverUser} who added the {@link CachedReaction reaction}
     * @param message The {@link CachedMessage message} onto which the {@link net.dv8tion.jda.api.entities.MessageReaction reaction}
     *                was added
     */
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
            futures.add(reactionService.removeReaction(message, cachedReaction.getEmote(), serverUser));
        }
        Long assignableRolePlaceId = assignableRolePlace.getId();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(aVoid ->
            self.updateStoredAssignableRoles(assignableRolePlaceId, serverUser, cachedReaction)
        ).exceptionally(throwable -> {
            log.error("Failed to add role or remove emote for assignable role place {}.", assignableRolePlaceId, throwable);
            return null;
        });
    }

    /**
     * Persists the {@link AssignableRole role} changes for the user who added a reaction in the database
     * @param assignableRolePlaceId The ID of the {@link AssignableRolePlace place}
     * @param serverUser The {@link ServerUser serverUser} who added the {@link net.dv8tion.jda.api.entities.MessageReaction reaction}
     * @param cachedReaction The {@link CachedReaction reaction} wich was added
     */
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

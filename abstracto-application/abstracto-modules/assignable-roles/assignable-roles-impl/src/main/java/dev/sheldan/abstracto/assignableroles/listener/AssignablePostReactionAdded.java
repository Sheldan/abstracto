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
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.ReactedAddedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private AssignableRolePlaceManagementService assignableRolePlaceManagementService;

    @Autowired
    private AssignablePostReactionAdded self;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private AssignableRoleManagementService assignableRoleManagementService;

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
                log.info("User {} added reaction to assignable role place {} in server {}. Handling added event.", userAdding.getUserReference().getId(), assignablePlacePost.getId(), event.getGuild().getId());
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
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (AssignableRole assignableRole : assignablePlacePost.getAssignableRoles()) {
            log.trace("Checking emote {} if it was reaction for assignable role place.", assignableRole.getEmote().getId());
            if (emoteService.isReactionEmoteAEmote(reactionEmote, assignableRole.getEmote())) {
                if(assignableRolePlace.getUniqueRoles()) {
                    log.trace("Assignable role place {} has unique roles configured. Removing existing reactions and roles.", assignableRolePlace.getId());
                    Optional<AssignedRoleUser> byUserInServer = assignedRoleUserManagementService.findByUserInServerOptional(userAdding);
                    byUserInServer.ifPresent(user -> futures.add(assignableRolePlaceService.removeExistingReactionsAndRoles(assignableRolePlace, user)));
                }

                Long assignableRoleId = assignableRole.getId();
                log.info("User added {} reaction {} and gets assignable role {} in server {}.", userAdding.getUserReference().getId(), assignableRole.getEmote().getId(), assignableRoleId, userAdding.getServerReference().getId());
                CompletableFuture<Void> roleAdditionFuture = assignableRoleServiceBean.assignAssignableRoleToUser(assignableRoleId, event.getMember());

                futures.add(CompletableFuture.allOf(roleAdditionFuture));
                validReaction = true;
                break;
            }
        }
        if(!validReaction) {
            log.trace("Reaction was not found in the configuration of assignable role place {}, removing reaction.", assignableRolePlace.getId());
            futures.add(reaction.removeReaction(event.getUser()).submit());
        }
        Long assignableRolePlaceId = assignableRolePlace.getId();
        Long userInServerId = userAdding.getUserInServerId();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(aVoid ->
            self.updateStoredAssignableRoles(assignableRolePlaceId, userInServerId, reactionEmote)
        );
    }

    private void updateStoredAssignableRoles(Long assignableRolePlaceId, Long userAdding, MessageReaction.ReactionEmote reactionEmote) {
        AssignableRolePlace place = assignableRolePlaceManagementService.findByPlaceId(assignableRolePlaceId);
        AUserInAServer userInAServer = userInServerManagementService.loadUser(userAdding);
        if(place.getUniqueRoles()) {
            log.trace("Assignable role place {} has unique roles. Deleting all existing references.", assignableRolePlaceId);
            assignableRoleServiceBean.clearAllRolesOfUserInPlace(place, userInAServer);
        }
        AssignableRole role = assignableRoleManagementService.getRoleForReactionEmote(reactionEmote, place);
        log.info("Adding role to assignable role {} to user {} in server {}.", role.getId(), userInAServer.getUserReference().getId(), userInAServer.getServerReference().getId());
        assignableRoleServiceBean.addRoleToUser(role.getId(), userInAServer);

    }

    @Override
    public FeatureEnum getFeature() {
        return AssignableRoleFeature.ASSIGNABLE_ROLES;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.HIGH;
    }
}

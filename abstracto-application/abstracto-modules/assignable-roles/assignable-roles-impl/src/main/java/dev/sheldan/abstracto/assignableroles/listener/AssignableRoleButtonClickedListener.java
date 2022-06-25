package dev.sheldan.abstracto.assignableroles.listener;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.exception.AssignableRoleNotFoundException;
import dev.sheldan.abstracto.assignableroles.exception.BoosterAssignableRolePlaceMemberNotBoostingException;
import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleConditionResult;
import dev.sheldan.abstracto.assignableroles.model.AssignableRolePlacePayload;
import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRolePlaceConditionModel;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlaceType;
import dev.sheldan.abstracto.assignableroles.model.database.AssignedRoleUser;
import dev.sheldan.abstracto.assignableroles.model.template.AssignableRoleSuccessNotificationModel;
import dev.sheldan.abstracto.assignableroles.service.AssignableRoleConditionServiceBean;
import dev.sheldan.abstracto.assignableroles.service.AssignableRolePlaceServiceBean;
import dev.sheldan.abstracto.assignableroles.service.AssignableRoleService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlaceManagementService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignedRoleUserManagementServiceBean;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AssignableRoleButtonClickedListener implements ButtonClickedListener {

    @Autowired
    private AssignableRolePlaceManagementService assignableRolePlaceManagementService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private AssignableRoleButtonClickedListener self;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private AssignableRoleService assignableRoleService;

    @Autowired
    private AssignedRoleUserManagementServiceBean assignedRoleUserManagementServiceBean;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private AssignableRoleConditionServiceBean assignableRoleConditionServiceBean;

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        ButtonInteractionEvent event = model.getEvent();
        Member member = event.getMember();
        if(event.getGuild() != null && member != null) {
            AssignableRolePlacePayload payload = (AssignableRolePlacePayload) model.getDeserializedPayload();
            AssignableRolePlace place = assignableRolePlaceManagementService.findByPlaceId(payload.getPlaceId());
            Guild guild = event.getGuild();
            List<Role> removedRoles = new ArrayList<>();
            Role roleById = guild.getRoleById(payload.getRoleId());
            Optional<AssignableRole> assignableRoleOptional = place
                    .getAssignableRoles()
                    .stream()
                    .filter(assignableRole -> assignableRole.getRole().getId().equals(payload.getRoleId()))
                    .findFirst();
            if(!assignableRoleOptional.isPresent()) {
                throw new AssignableRoleNotFoundException(payload.getRoleId());
            }
            if(roleById != null) {
                boolean memberHasRole = member
                        .getRoles()
                        .stream()
                        .anyMatch(memberRole -> memberRole.getIdLong() == payload.getRoleId());
                if(!memberHasRole) {
                    if(place.getType().equals(AssignableRolePlaceType.BOOSTER) && member.getTimeBoosted() == null) {
                        assignableRoleService.assignableRoleConditionFailure();
                        throw new BoosterAssignableRolePlaceMemberNotBoostingException();
                    }
                    AssignableRole assignableRole = assignableRoleOptional.get();
                    AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
                    if(!assignableRole.getConditions().isEmpty()) {
                        log.debug("Evaluating {} conditions for assignable role {}.", assignableRole.getConditions().size(), assignableRole.getId());
                        AssignableRoleConditionResult conditionResult =
                                assignableRoleConditionServiceBean.evaluateConditions(assignableRole.getConditions(), aUserInAServer, roleById);
                        if(!conditionResult.getFulfilled()) {
                            log.info("One condition failed to be fulfilled - notifying user.");
                            self.notifyUserAboutConditionFail(model, event.getInteraction(), conditionResult.getModel());
                            assignableRoleService.assignableRoleConditionFailure();
                            return ButtonClickedListenerResult.ACKNOWLEDGED;
                        }
                    }
                    CompletableFuture<Void> removalFuture;
                    if(place.getUniqueRoles()) {
                        Optional<AssignedRoleUser> assignedRoleUserOptional = assignedRoleUserManagementServiceBean.findByUserInServerOptional(aUserInAServer);
                        if(assignedRoleUserOptional.isPresent()) {
                            AssignedRoleUser roleUser = assignedRoleUserOptional.get();
                            List<Role> rolesToRemove = roleUser
                                    .getRoles()
                                    .stream()
                                    .filter(roleOfUser -> roleOfUser.getAssignablePlace().equals(place))
                                    .map(roleOfUser -> guild.getRoleById(roleOfUser.getRole().getId()))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                            log.info("Removing {} because of unique role configuration in place {}.", rolesToRemove.size(), place.getId());
                            removedRoles.addAll(rolesToRemove);
                            List<CompletableFuture<Void>> removalFutures = new ArrayList<>();
                            rolesToRemove.forEach(roleToRemove -> removalFutures.add(roleService.removeRoleFromUserAsync(member, roleToRemove)));
                            removalFuture = new CompletableFutureList<>(removalFutures).getMainFuture();
                        } else {
                            removalFuture = CompletableFuture.completedFuture(null);
                        }
                    } else {
                        removalFuture = CompletableFuture.completedFuture(null);
                    }
                    CompletableFuture<Void> roleAdditionFuture = assignableRoleService.assignAssignableRoleToUser(roleById, member);
                    CompletableFuture.allOf(removalFuture, roleAdditionFuture).whenComplete((unused, throwable) -> {
                        if(throwable != null) {
                            log.error("Failed to either add or remove roles for assignable role place {} in server {}.", payload.getPlaceId(), guild.getIdLong());
                        }
                        if(!roleAdditionFuture.isCompletedExceptionally()) {
                            log.info("Added role {} to member {} in server {} for assignable role interaction {} on component {}.",
                                    roleById.getId(), member.getId(), guild.getIdLong(), event.getInteraction().getId(), event.getComponentId());
                            self.notifyUser(model, true, roleById, event.getInteraction(), removedRoles).thenAccept(unused1 -> {
                                log.info("Persisting adding assignable role update for user {} in server {} of role {}.", member.getIdLong(), guild.getIdLong(), roleById.getId());
                                self.persistAssignableUser(member, payload, false);
                            });
                        }
                    }).exceptionally(throwable -> {
                        log.error("Failed to perform role change in assignable role place.", throwable);
                        return null;
                    });
                } else {
                    assignableRoleService.removeAssignableRoleFromUser(roleById, member)
                        .thenAccept(unused -> {
                            self.notifyUser(model, false, roleById, event.getInteraction(), new ArrayList<>());
                            log.info("Removed role {} from member {} in server {} for assignable role interaction {} on component {}.",
                                    roleById.getId(), member.getId(), guild.getIdLong(), event.getInteraction().getId(), event.getComponentId());
                        }).thenAccept(unused -> {
                            log.info("Persisting remove assignable role update for user {} in server {} of role {}.", member.getIdLong(), guild.getIdLong(), roleById.getId());
                            self.persistAssignableUser(member, payload, true);
                    });
                }
            } else {
                log.warn("Role {} is not available to be assigned in assignable role place {} in server {}. Component {} failed.",
                        payload.getRoleId(), payload.getPlaceId(), guild.getIdLong(), event.getComponentId());
                throw new AssignableRoleNotFoundException(payload.getRoleId());
            }
        }
        return ButtonClickedListenerResult.ACKNOWLEDGED;
    }

    @Transactional
    public void persistAssignableUser(Member member, AssignableRolePlacePayload payload, boolean removeRole){
        AssignableRolePlace place = assignableRolePlaceManagementService.findByPlaceId(payload.getPlaceId());
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
        if(place.getUniqueRoles() && !removeRole) {
            assignableRoleService.clearAllRolesOfUserInPlace(place, aUserInAServer);
        }
        Optional<AssignableRole> assignableRoleOptional = place
                .getAssignableRoles()
                .stream()
                .filter(assignableRole -> assignableRole.getRole().getId().equals(payload.getRoleId()))
                .findFirst();
        if(assignableRoleOptional.isPresent()) {
            if(removeRole) {
                assignableRoleService.removeRoleFromUser(assignableRoleOptional.get(), aUserInAServer);
            } else {
                assignableRoleService.addRoleToUser(assignableRoleOptional.get(), aUserInAServer);
            }
        }
    }

    @Transactional
    public CompletableFuture<Void> notifyUser(ButtonClickedListenerModel model, boolean roleAdded, Role role, ButtonInteraction buttonInteraction, List<Role> removedRoles) {
        log.info("Notifying user {} in server {} in channel {} about role change with role {}.",
                buttonInteraction.getUser().getIdLong(), buttonInteraction.getGuild().getIdLong(), buttonInteraction.getChannel().getIdLong(), role.getId());
        AssignableRoleSuccessNotificationModel notificationModel = AssignableRoleSuccessNotificationModel
                .builder()
                .added(roleAdded)
                .removedRoles(removedRoles)
                .role(role)
                .build();
        return FutureUtils.toSingleFutureGeneric(
                interactionService.sendMessageToInteraction("assignable_role_success_notification", notificationModel, buttonInteraction.getHook()))                ;
    }

    @Transactional
    public CompletableFuture<Void> notifyUserAboutConditionFail(ButtonClickedListenerModel model, ButtonInteraction buttonInteraction,
                                                                AssignableRolePlaceConditionModel conditionModel) {
        log.info("Notifying user {} in server {} in channel {} about failed condition.", buttonInteraction.getUser().getIdLong(),
                buttonInteraction.getGuild().getIdLong(), buttonInteraction.getChannel().getIdLong());
        return FutureUtils.toSingleFutureGeneric(
                interactionService.sendMessageToInteraction("assignable_role_condition_notification", conditionModel, buttonInteraction.getHook()))                ;
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return AssignableRolePlaceServiceBean.ASSIGNABLE_ROLE_COMPONENT_ORIGIN.equals(model.getOrigin()) && model.getEvent().isFromGuild();
    }

    @Override
    public FeatureDefinition getFeature() {
        return AssignableRoleFeatureDefinition.ASSIGNABLE_ROLES;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }
}

package dev.sheldan.abstracto.assignableroles.listener;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlaceType;
import dev.sheldan.abstracto.assignableroles.model.database.AssignedRoleUser;
import dev.sheldan.abstracto.assignableroles.service.AssignableRoleService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRoleManagementService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignedRoleUserManagementService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMemberBoostTimeUpdateListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.BoostTimeUpdatedModel;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
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
public class AssignableRolePlaceBoostTimeUpdateListener implements AsyncMemberBoostTimeUpdateListener {

    @Autowired
    private AssignedRoleUserManagementService assignedRoleUserManagementService;

    @Autowired
    private AssignableRoleManagementService assignableRoleManagementService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private AssignableRoleService assignableRoleService;

    @Autowired
    private AssignableRolePlaceBoostTimeUpdateListener self;

    @Override
    public DefaultListenerResult execute(BoostTimeUpdatedModel model) {
        Member member = model.getMember();
        if(member.getTimeBoosted() == null) {
            removeAssignedBoosterRoles(member);
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
    }

    private void removeAssignedBoosterRoles(Member member) {
        log.info("Member {} in server {} stopped boosting.", member.getIdLong(), member.getGuild().getIdLong());
        ServerUser serverUser = ServerUser.fromMember(member);
        Optional<AssignedRoleUser> assignedRoleUserOptional = assignedRoleUserManagementService.findByUserInServerOptional(serverUser);
        if(assignedRoleUserOptional.isPresent()) {
            AssignedRoleUser assignedRoleUser = assignedRoleUserOptional.get();
            List<AssignableRole> boosterRoles = assignableRoleManagementService.getAssignableRolesFromAssignableUserWithPlaceType(assignedRoleUser, AssignableRolePlaceType.BOOSTER);
            if(!boosterRoles.isEmpty()) {
                log.info("Removing {} assignable role mappings.", boosterRoles.size());
                Guild guild = member.getGuild();
                List<Role> actualRolesToDelete = boosterRoles
                        .stream()
                        .map(assignableRole -> guild.getRoleById(assignableRole.getRole().getId()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                log.debug("Which translated to {} roles in reality.", actualRolesToDelete.size());
                List<CompletableFuture<Void>> list = new ArrayList<>();
                actualRolesToDelete.forEach(role -> list.add(roleService.removeRoleFromUserAsync(member, role)));
                FutureUtils.toSingleFutureGeneric(list)
                .thenAccept(unused -> self.clearPersistedBoosterAssignableRoles(member))
                .exceptionally(throwable -> {
                    log.warn("One or more roles might have failed to remove. ", throwable);
                    self.clearPersistedBoosterAssignableRoles(member);
                    return null;
                });
            } else {
                log.info("Member {} in server {} did not have boost roles - doing nothing.", member.getIdLong(), member.getGuild().getIdLong());
            }
        } else {
            log.info("Member (ID {}) in server (ID: {}), who was not tracked via assignable roles, stopped boosting - doing nothing.",
                    member.getIdLong(), member.getGuild().getIdLong());
        }
    }

    @Transactional
    public void clearPersistedBoosterAssignableRoles(Member member) {
        ServerUser serverUser = ServerUser.fromMember(member);
        Optional<AssignedRoleUser> assignedRoleUserOptional = assignedRoleUserManagementService.findByUserInServerOptional(serverUser);
        if(assignedRoleUserOptional.isPresent()) {
            AssignedRoleUser assignedRoleUser = assignedRoleUserOptional.get();
            List<AssignableRole> boosterRoles = assignableRoleManagementService.getAssignableRolesFromAssignableUserWithPlaceType(assignedRoleUser, AssignableRolePlaceType.BOOSTER);
            assignableRoleService.removeAssignableRolesFromAssignableRoleUser(boosterRoles, assignedRoleUser);
        } else {
            log.warn("No assigned role user found for member {} in server {}.", member.getIdLong(), member.getGuild().getIdLong());
        }
    }

    @Override
    public FeatureDefinition getFeature() {
        return AssignableRoleFeatureDefinition.ASSIGNABLE_ROLES;
    }
}

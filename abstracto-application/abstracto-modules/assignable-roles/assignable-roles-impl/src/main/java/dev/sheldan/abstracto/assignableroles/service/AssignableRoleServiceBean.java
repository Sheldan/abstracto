package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.exception.AssignableRoleNotFoundException;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignedRoleUser;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRoleManagementServiceBean;
import dev.sheldan.abstracto.assignableroles.service.management.AssignedRoleUserManagementService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignedRoleUserManagementServiceBean;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AssignableRoleServiceBean implements AssignableRoleService {

    @Autowired
    private RoleService roleService;

    @Autowired
    private AssignedRoleUserManagementService assignedRoleUserManagementService;

    @Autowired
    private AssignableRoleManagementServiceBean assignableRoleManagementServiceBean;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private AssignedRoleUserManagementServiceBean assignedRoleUserManagementServiceBean;

    @Autowired
    private AssignableRoleServiceBean self;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MetricService metricService;

    public static final String ASSIGNABLE_ROLES_METRIC = "assignable.roles";
    public static final String ACTION = "action";
    private static final CounterMetric ASSIGNABLE_ROLES_ASSIGNED =
            CounterMetric
                    .builder()
                    .name(ASSIGNABLE_ROLES_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(ACTION, "assigned")))
                    .build();

    private static final CounterMetric ASSIGNABLE_ROLES_REMOVED =
            CounterMetric
                    .builder()
                    .name(ASSIGNABLE_ROLES_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(ACTION, "removed")))
                    .build();

    private static final CounterMetric ASSIGNABLE_ROLES_CONDITION_FAILED =
            CounterMetric
                    .builder()
                    .name(ASSIGNABLE_ROLES_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(ACTION, "condition")))
                    .build();

    @Override
    public CompletableFuture<Void> assignAssignableRoleToUser(Long assignableRoleId, Member member) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        log.info("Assigning role {} to member {} in server {}.", assignableRoleId, member.getId(), member.getGuild().getId());
        metricService.incrementCounter(ASSIGNABLE_ROLES_ASSIGNED);
        return roleService.addRoleToMemberAsync(member, role.getRole());
    }

    @Override
    public CompletableFuture<Void> assignAssignableRoleToUser(Role role, Member member) {
        return assignRoleToUser(role.getIdLong(), member);
    }

    @Override
    public void assignableRoleConditionFailure() {
        metricService.incrementCounter(ASSIGNABLE_ROLES_CONDITION_FAILED);
    }

    private CompletableFuture<Void> assignRoleToUser(Long roleId, Member member) {
        metricService.incrementCounter(ASSIGNABLE_ROLES_ASSIGNED);
        return roleService.addRoleToMemberAsync(member, roleId);
    }

    @Override
    public void clearAllRolesOfUserInPlace(AssignableRolePlace place, AUserInAServer userInAServer) {
        Optional<AssignedRoleUser> userOptional = assignedRoleUserManagementServiceBean.findByUserInServerOptional(userInAServer);
        userOptional.ifPresent(assignedRoleUser -> {
            log.info("Clearing all {} assignable roles in place {} for user {} in server {}.",
                    assignedRoleUser.getRoles().size(), place.getId(), userInAServer.getUserReference().getId(), userInAServer.getServerReference().getId());
            assignedRoleUser.getRoles().forEach(assignableRole -> {
                if(assignableRole.getAssignablePlace().equals(place)) {
                    assignableRole.getAssignedUsers().remove(assignedRoleUser);
                }
            });
            assignedRoleUser.getRoles().removeIf(assignableRole -> assignableRole.getAssignablePlace().equals(place));
        });

        if(!userOptional.isPresent()) {
            log.info("User {} was not yet stored as an assignable role user in server {} - nothing to clear.",
                    userInAServer.getUserReference().getId(), place.getServer().getId());
        }
    }

    @Override
    public CompletableFuture<Void> removeAssignableRoleFromUser(AssignableRole assignableRole, Member member) {
        log.info("Removing assignable role {} from user {} in server {}.", assignableRole.getId(), member.getId(), member.getGuild().getId());
        return removeRoleFromUser(assignableRole.getRole().getId(), member);
    }

    @Override
    public CompletableFuture<Void> removeAssignableRoleFromUser(Role role, Member member) {
        return removeRoleFromUser(role.getIdLong(), member);
    }

    private CompletableFuture<Void> removeRoleFromUser(Long roleId, Member member) {
        metricService.incrementCounter(ASSIGNABLE_ROLES_REMOVED);
        return roleService.removeRoleFromMemberAsync(member, roleId);
    }

    @Override
    @Transactional
    public CompletableFuture<Void> removeAssignableRoleFromUser(Long assignableRoleId, Member member) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        return self.removeAssignableRoleFromUser(role, member);
    }

    @Override
    public void addRoleToUser(AssignableRole assignableRole, AUserInAServer aUserInAServer) {
        log.info("Persisting storing adding assignable role {} to user {} in server {}.",
                assignableRole.getId(), aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId());
        assignedRoleUserManagementServiceBean.addAssignedRoleToUser(assignableRole, aUserInAServer);
    }

    @Override
    public void removeRoleFromUser(AssignableRole assignableRole, AUserInAServer aUserInAServer) {
        log.info("Persisting storing removing assignable role {} to user {} in server {}.",
                assignableRole.getId(), aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId());
        assignedRoleUserManagementServiceBean.removeAssignedRoleFromUser(assignableRole, aUserInAServer);
    }

    @Override
    public AssignableRole getAssignableRoleInPlace(AssignableRolePlace place, Role role) {
        return getAssignableRoleInPlace(place, role.getIdLong());
    }

    @Override
    public AssignableRole getAssignableRoleInPlace(AssignableRolePlace place, ARole role) {
        return getAssignableRoleInPlace(place, role.getId());
    }

    @Override
    public AssignableRole getAssignableRoleInPlace(AssignableRolePlace place, Long roleId) {
        for (AssignableRole assignableRole : place.getAssignableRoles()) {
            if (assignableRole.getRole().getId().equals(roleId)) {
                return assignableRole;
            }
        }
        throw new AssignableRoleNotFoundException(roleId);
    }

    @Override
    public void removeAssignableRolesFromAssignableRoleUser(List<AssignableRole> roles, AssignedRoleUser roleUser) {
        log.info("Removing {} assignable roles from user {} in server {}.", roles.size(), roleUser.getUser().getUserReference().getId(),
                roleUser.getUser().getServerReference().getId());
        roles.forEach(assignableRole -> assignedRoleUserManagementServiceBean.removeAssignedRoleFromUser(assignableRole, roleUser));
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(ASSIGNABLE_ROLES_ASSIGNED, "Assignable roles assigned.");
        metricService.registerCounter(ASSIGNABLE_ROLES_REMOVED, "Assignable roles removed.");
        metricService.registerCounter(ASSIGNABLE_ROLES_CONDITION_FAILED, "Assignable roles failed to assign because of condition.");
    }
}

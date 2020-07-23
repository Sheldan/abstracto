package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRoleManagementServiceBean;
import dev.sheldan.abstracto.assignableroles.service.management.AssignedRoleUserManagementService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignedRoleUserManagementServiceBean;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Component
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

    @Override
    public CompletableFuture<Void> assignAssignableRoleToUser(Long assignableRoleId, Member toAdd) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        return roleService.addRoleToMemberFuture(toAdd, role.getRole()).thenApply(aVoid -> {
            self.persistRoleAssignment(assignableRoleId, toAdd);
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> removeAssignableRoleFromUser(AssignableRole assignableRole, Member member) {
        Long assignableRoleId = assignableRole.getId();
        return roleService.removeRoleFromMemberFuture(member, assignableRole.getRole()).thenApply(aVoid -> {
            self.persistRoleRemoval(assignableRoleId, member);
            return null;
        });
    }

    @Transactional
    public void persistRoleAssignment(Long assignableRoleId, Member member) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(member);
        assignedRoleUserManagementServiceBean.addAssignedRoleToUser(role, aUserInAServer);
    }

    @Transactional
    public void persistRoleRemoval(Long assignableRoleId, Member member) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(member);
        assignedRoleUserManagementServiceBean.removeAssignedRoleFromUser(role, aUserInAServer);
    }
}

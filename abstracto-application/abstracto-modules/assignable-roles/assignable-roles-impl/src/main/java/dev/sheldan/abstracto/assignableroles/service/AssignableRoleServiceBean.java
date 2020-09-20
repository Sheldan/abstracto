package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser;
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
        return roleService.addRoleToMemberFuture(toAdd, role.getRole());
    }

    @Override
    public void clearAllRolesOfUserInPlace(AssignableRolePlace place, AUserInAServer userInAServer) {
        AssignedRoleUser user = assignedRoleUserManagementServiceBean.findByUserInServer(userInAServer);
        user.getRoles().forEach(assignableRole -> {
            if(assignableRole.getAssignablePlace().equals(place)) {
                assignableRole.getAssignedUsers().remove(user);
            }
        });
        user.getRoles().removeIf(assignableRole -> assignableRole.getAssignablePlace().equals(place));
    }

    @Override
    public CompletableFuture<Void> fullyAssignAssignableRoleToUser(Long assignableRoleId, Member toAdd) {
        return this.assignAssignableRoleToUser(assignableRoleId, toAdd).thenAccept(aVoid ->
            self.addRoleToUser(assignableRoleId, toAdd)
        );
    }

    @Override
    public CompletableFuture<Void> removeAssignableRoleFromUser(AssignableRole assignableRole, Member member) {
        return roleService.removeRoleFromMemberFuture(member, assignableRole.getRole());
    }

    @Override
    public CompletableFuture<Void> fullyRemoveAssignableRoleFromUser(AssignableRole assignableRole, Member member) {
        Long assignableRoleId = assignableRole.getId();
        return this.removeAssignableRoleFromUser(assignableRole, member).thenAccept(aVoid ->
            self.removeRoleFromUser(assignableRoleId, member)
        );
    }

    @Transactional
    public void addRoleToUser(Long assignableRoleId, AUserInAServer aUserInAServer) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        addRoleToUser(role, aUserInAServer);
    }

    @Override
    public void addRoleToUser(AssignableRole assignableRole, AUserInAServer aUserInAServer) {
        assignedRoleUserManagementServiceBean.addAssignedRoleToUser(assignableRole, aUserInAServer);
    }

    @Override
    public void removeRoleFromUser(AssignableRole assignableRole, AUserInAServer aUserInAServer) {
        assignedRoleUserManagementServiceBean.removeAssignedRoleFromUser(assignableRole, aUserInAServer);
    }

    @Transactional
    public void addRoleToUser(Long assignableRoleId, Member member) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(member);
        assignedRoleUserManagementServiceBean.addAssignedRoleToUser(role, aUserInAServer);
    }

    @Transactional
    public void removeRoleFromUser(Long assignableRoleId, AUserInAServer aUserInAServer) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        removeRoleFromUser(role, aUserInAServer);
    }

    @Transactional
    public void removeRoleFromUser(Long assignableRoleId, Member member) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(member);
        assignedRoleUserManagementServiceBean.removeAssignedRoleFromUser(role, aUserInAServer);
    }
}

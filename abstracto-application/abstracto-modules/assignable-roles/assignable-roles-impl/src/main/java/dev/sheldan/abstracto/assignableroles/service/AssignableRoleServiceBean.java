package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRoleManagementServiceBean;
import dev.sheldan.abstracto.assignableroles.service.management.AssignedRoleUserManagementService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignedRoleUserManagementServiceBean;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private BotService botService;

    @Override
    public CompletableFuture<Void> assignAssignableRoleToUser(Long assignableRoleId, Member toAdd) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        log.info("Assigning role {} to member {} in server {}.", assignableRoleId, toAdd.getId(), toAdd.getGuild().getId());
        return roleService.addRoleToMemberFuture(toAdd, role.getRole());
    }

    @Override
    public void clearAllRolesOfUserInPlace(AssignableRolePlace place, AUserInAServer userInAServer) {
        AssignedRoleUser user = assignedRoleUserManagementServiceBean.findByUserInServer(userInAServer);
        log.info("Clearing all {} assignable roles in place {} for user {} in server {}.",
                user.getRoles().size(), place.getId(), userInAServer.getUserReference().getId(), userInAServer.getServerReference().getId());
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
        log.info("Removing assignable role {} from user {} in server {}.", assignableRole.getId(), member.getId(), member.getGuild().getId());
        return roleService.removeRoleFromMemberAsync(member, assignableRole.getRole());
    }

    @Override
    @Transactional
    public CompletableFuture<Void> removeAssignableRoleFromUser(Long assignableRoleId, Member member) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        return self.removeAssignableRoleFromUser(role, member);
    }

    @Override
    public CompletableFuture<Void> removeAssignableRoleFromUser(AssignableRole assignableRole, AUserInAServer aUserInAServer) {
        Long assignableRoleId = assignableRole.getId();
        return botService.getMemberInServerAsync(aUserInAServer).thenCompose(member ->
            self.removeAssignableRoleFromUser(assignableRoleId, member)
        );
    }

    @Override
    public CompletableFuture<Void> fullyRemoveAssignableRoleFromUser(AssignableRole assignableRole, Member member) {
        Long assignableRoleId = assignableRole.getId();
        return this.removeAssignableRoleFromUser(assignableRole, member).thenAccept(aVoid ->
            self.persistRoleRemovalFromUser(assignableRoleId, member)
        );
    }

    @Transactional
    public void addRoleToUser(Long assignableRoleId, AUserInAServer aUserInAServer) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        addRoleToUser(role, aUserInAServer);
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

    @Transactional
    public void addRoleToUser(Long assignableRoleId, Member member) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(member);
        addRoleToUser(role, aUserInAServer);
    }

    @Transactional
    public void removeRoleFromUser(Long assignableRoleId, AUserInAServer aUserInAServer) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        removeRoleFromUser(role, aUserInAServer);
    }

    @Transactional
    public void persistRoleRemovalFromUser(Long assignableRoleId, Member member) {
        AssignableRole role = assignableRoleManagementServiceBean.getByAssignableRoleId(assignableRoleId);
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(member);
        removeRoleFromUser(role, aUserInAServer);
    }
}
package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.exception.AssignedUserNotFoundException;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignedRoleUser;
import dev.sheldan.abstracto.assignableroles.repository.AssignedRoleUserRepository;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class AssignedRoleUserManagementServiceBean implements AssignedRoleUserManagementService {

    @Autowired
    private AssignedRoleUserRepository repository;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public void addAssignedRoleToUser(AssignableRole assignableRole, AUserInAServer aUserInAServer) {
        Optional<AssignedRoleUser> optional = findByUserInServerOptional(aUserInAServer);
        log.info("Adding assignable role {} to user {} in server {} because of assignable role place {}.",
                assignableRole.getId(), aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId(),
                assignableRole.getAssignablePlace().getId());
        AssignedRoleUser user = optional.orElseGet(() -> createAssignedRoleUser(aUserInAServer));
        assignableRole.getAssignedUsers().add(user);
        user.getRoles().add(assignableRole);
    }

    @Override
    public void removeAssignedRoleFromUser(AssignableRole assignableRole, AUserInAServer aUserInAServer) {
        log.info("Removing assignable role {} from user {} in server {} in assignable role place {}.",
                assignableRole.getId(), aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId(),
                assignableRole.getAssignablePlace().getId());
        AssignedRoleUser user = findByUserInServer(aUserInAServer);
        removeAssignedRoleFromUser(assignableRole, user);
    }

    @Override
    public void removeAssignedRoleFromUsers(AssignableRole assignableRole, List<AssignedRoleUser> users) {
        log.info("Clearing all assignable role {} for {} users.", assignableRole.getId(), users.size());
        assignableRole.getAssignedUsers().removeAll(users);
        users.forEach(roleUser -> roleUser.getRoles().remove(assignableRole));
    }

    @Override
    public void removeAssignedRoleFromUsers(AssignableRole assignableRole) {
        removeAssignedRoleFromUsers(assignableRole, assignableRole.getAssignedUsers());
    }

    @Override
    public void removeAssignedRoleFromUser(AssignableRole assignableRole, AssignedRoleUser user) {
        assignableRole.getAssignedUsers().remove(user);
        user.getRoles().remove(assignableRole);
    }

    @Override
    public AssignedRoleUser createAssignedRoleUser(AUserInAServer aUserInAServer) {
        log.info("Creating assigned role user for user {} in server {}.", aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId());
        AssignedRoleUser newUser = AssignedRoleUser.builder().user(aUserInAServer).id(aUserInAServer.getUserInServerId()).build();
        return repository.save(newUser);
    }

    @Override
    public Optional<AssignedRoleUser> findByUserInServerOptional(AUserInAServer aUserInAServer) {
        return repository.findById(aUserInAServer.getUserInServerId());
    }

    @Override
    public Optional<AssignedRoleUser> findByUserInServerOptional(ServerUser serverUser) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(serverUser);
        return findByUserInServerOptional(aUserInAServer);
    }

    @Override
    public AssignedRoleUser findByUserInServer(AUserInAServer aUserInAServer) {
        return findByUserInServerOptional(aUserInAServer).orElseThrow(() -> new AssignedUserNotFoundException(aUserInAServer));
    }
}

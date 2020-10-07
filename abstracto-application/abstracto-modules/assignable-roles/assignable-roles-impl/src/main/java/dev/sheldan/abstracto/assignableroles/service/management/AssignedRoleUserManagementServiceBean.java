package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.exceptions.AssignedUserNotFoundException;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser;
import dev.sheldan.abstracto.assignableroles.repository.AssignedRoleUserRepository;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class AssignedRoleUserManagementServiceBean implements AssignedRoleUserManagementService {

    @Autowired
    private AssignedRoleUserRepository repository;

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
    public void clearAllAssignedRolesOfUser(AUserInAServer userInAServer) {
        AssignedRoleUser user = findByUserInServer(userInAServer);
        log.info("Clearing all assignable roles for user {} in server {}.", userInAServer.getUserReference().getId(), userInAServer.getServerReference().getId());
        user.getRoles().forEach(assignableRole ->
            assignableRole.getAssignedUsers().remove(user)
        );
        user.getRoles().clear();
    }

    @Override
    public boolean doesAssignedRoleUserExist(AUserInAServer aUserInAServer) {
        return repository.existsById(aUserInAServer.getUserInServerId());
    }

    @Override
    public Optional<AssignedRoleUser> findByUserInServerOptional(AUserInAServer aUserInAServer) {
        return repository.findById(aUserInAServer.getUserInServerId());
    }

    @Override
    public AssignedRoleUser findByUserInServer(AUserInAServer aUserInAServer) {
        return findByUserInServerOptional(aUserInAServer).orElseThrow(() -> new AssignedUserNotFoundException(aUserInAServer));
    }
}

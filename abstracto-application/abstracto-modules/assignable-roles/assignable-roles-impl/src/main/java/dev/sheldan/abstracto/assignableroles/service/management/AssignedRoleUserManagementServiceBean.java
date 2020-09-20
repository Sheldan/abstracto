package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.exceptions.AssignedUserNotFoundException;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser;
import dev.sheldan.abstracto.assignableroles.repository.AssignedRoleUserRepository;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AssignedRoleUserManagementServiceBean implements AssignedRoleUserManagementService {

    @Autowired
    private AssignedRoleUserRepository repository;

    @Override
    public void addAssignedRoleToUser(AssignableRole assignableRole, AUserInAServer aUserInAServer) {
        Optional<AssignedRoleUser> optional = findByUserInServerOptional(aUserInAServer);
        AssignedRoleUser user = optional.orElseGet(() -> createAssignedRoleUser(aUserInAServer));
        assignableRole.getAssignedUsers().add(user);
        user.getRoles().add(assignableRole);
    }

    @Override
    public void removeAssignedRoleFromUser(AssignableRole assignableRole, AUserInAServer aUserInAServer) {
        AssignedRoleUser user = findByUserInServer(aUserInAServer);
        assignableRole.getAssignedUsers().remove(user);
        user.getRoles().remove(assignableRole);
    }

    @Override
    public AssignedRoleUser createAssignedRoleUser(AUserInAServer aUserInAServer) {
        AssignedRoleUser newUser = AssignedRoleUser.builder().user(aUserInAServer).id(aUserInAServer.getUserInServerId()).build();
        return repository.save(newUser);
    }

    @Override
    public void clearAllAssignedRolesOfUser(AUserInAServer userInAServer) {
        AssignedRoleUser user = findByUserInServer(userInAServer);
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

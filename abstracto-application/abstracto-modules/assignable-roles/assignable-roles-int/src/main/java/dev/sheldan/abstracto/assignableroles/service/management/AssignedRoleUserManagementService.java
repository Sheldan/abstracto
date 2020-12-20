package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;

import java.util.Optional;

public interface AssignedRoleUserManagementService {
    void addAssignedRoleToUser(AssignableRole assignableRole, AUserInAServer aUserInAServer);
    void removeAssignedRoleFromUser(AssignableRole assignableRole, AUserInAServer aUserInAServer);
    AssignedRoleUser createAssignedRoleUser(AUserInAServer aUserInAServer);
    void clearAllAssignedRolesOfUser(AUserInAServer userInAServer);
    boolean doesAssignedRoleUserExist(AUserInAServer aUserInAServer);
    Optional<AssignedRoleUser> findByUserInServerOptional(AUserInAServer aUserInAServer);
    Optional<AssignedRoleUser> findByUserInServerOptional(ServerUser serverUser);
    AssignedRoleUser findByUserInServer(AUserInAServer aUserInAServer);
}

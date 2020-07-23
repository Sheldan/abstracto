package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.List;
import java.util.Optional;

public interface AssignableRolePlaceManagementService {
    AssignableRolePlace createPlace(AServer server, String name, AChannel channel, String text);
    boolean doesPlaceExist(AServer server, String name);
    AssignableRolePlace findByServerAndKey(AServer server, String name);
    Optional<AssignableRolePlace> findByPlaceId(Long id);
    void moveAssignableRolePlace(AServer server, String name, AChannel newChannel);
    void changeAssignableRolePlaceDescription(AServer server, String name, String newDescription);
    void deleteAssignablePlace(AssignableRolePlace place);
    List<AssignableRolePlace> findAllByServer(AServer server);

}

package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlaceType;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.List;
import java.util.Optional;

public interface AssignableRolePlaceManagementService {

    AssignableRolePlace createPlace(String name, AChannel channel, String text, AssignableRolePlaceType type);

    boolean doesPlaceExist(AServer server, String name);

    AssignableRolePlace findByServerAndKey(AServer server, String name);

    Optional<AssignableRolePlace> findByPlaceIdOptional(Long id);

    AssignableRolePlace findByPlaceId(Long id);

    void moveAssignableRolePlace(String name, AChannel newChannel);

    void changeAssignableRolePlaceDescription(AServer server, String name, String newDescription);

    void deleteAssignablePlace(AssignableRolePlace place);

    List<AssignableRolePlace> findAllByServer(AServer server);

}

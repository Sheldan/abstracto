package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.model.database.MuteRole;

import java.util.List;

public interface MuteRoleManagementService {
    MuteRole retrieveMuteRoleForServer(AServer server);
    MuteRole createMuteRoleForServer(AServer server, ARole role);
    List<MuteRole> retrieveMuteRolesForServer(AServer server);
    MuteRole setMuteRoleForServer(AServer server, ARole role);
    boolean muteRoleForServerExists(AServer server);
}

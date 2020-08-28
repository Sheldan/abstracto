package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;

public interface UserInServerService {
    FullUserInServer getFullUser(AUserInAServer aUserInAServer);
}

package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;

public interface UserInServerService {
    FullUser getFullUser(AUserInAServer aUserInAServer);
}

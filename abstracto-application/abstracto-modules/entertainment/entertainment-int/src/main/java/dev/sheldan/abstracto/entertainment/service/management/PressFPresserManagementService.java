package dev.sheldan.abstracto.entertainment.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.entertainment.model.database.PressF;
import dev.sheldan.abstracto.entertainment.model.database.PressFPresser;

public interface PressFPresserManagementService {
    PressFPresser addPresser(PressF pressF, AUserInAServer presser);
    boolean didUserAlreadyPress(PressF pressF, AUserInAServer aUserInAServer);
}

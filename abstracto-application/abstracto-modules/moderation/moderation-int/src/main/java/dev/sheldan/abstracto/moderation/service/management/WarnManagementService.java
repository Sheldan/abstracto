package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;

public interface WarnManagementService {
    Warning createWarning(AUserInAServer warnedAUser, AUserInAServer warningAUser, String reason);
}

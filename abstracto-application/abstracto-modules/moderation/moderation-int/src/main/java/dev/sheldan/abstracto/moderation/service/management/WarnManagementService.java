package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.moderation.models.Warning;
import dev.sheldan.abstracto.core.models.AUserInAServer;

public interface WarnManagementService {
    Warning createWarning(AUserInAServer warnedAUser, AUserInAServer warningAUser, String reason);
}

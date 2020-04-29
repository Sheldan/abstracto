package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;

import java.time.Instant;
import java.util.List;

public interface WarnManagementService {
    Warning createWarning(AUserInAServer warnedAUser, AUserInAServer warningAUser, String reason);
    List<Warning> getActiveWarningsInServerOlderThan(AServer server, Instant date);
}

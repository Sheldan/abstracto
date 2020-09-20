package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WarnManagementService {
    Warning createWarning(AUserInAServer warnedAUser, AUserInAServer warningAUser, String reason, Long warnId);
    List<Warning> getActiveWarningsInServerOlderThan(AServer server, Instant date);
    Long getTotalWarnsForUser(AUserInAServer aUserInAServer);
    List<Warning> getAllWarnsForUser(AUserInAServer aUserInAServer);
    List<Warning> getAllWarningsOfServer(AServer server);
    Long getActiveWarnsForUser(AUserInAServer aUserInAServer);
    Optional<Warning> findById(Long id, Long serverId);
    void deleteWarning(Warning warn);
}

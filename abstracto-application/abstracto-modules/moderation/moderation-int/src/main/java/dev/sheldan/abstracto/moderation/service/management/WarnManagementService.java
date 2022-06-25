package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.Warning;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WarnManagementService {
    Warning createWarning(AUserInAServer warnedAUser, AUserInAServer warningAUser, String reason, Long warnId);
    List<Warning> getActiveWarningsInServerOlderThan(AServer server, Instant date);
    List<Warning> getActiveWarningsInServerYoungerThan(AServer server, Instant date);
    List<Warning> getActiveWarningsFromUserYoungerThan(AUserInAServer user, Instant date);
    Long getTotalWarnsForUser(AUserInAServer aUserInAServer);
    List<Warning> getAllWarnsForUser(AUserInAServer aUserInAServer);
    List<Warning> getAllWarningsOfServer(AServer server);
    Long getActiveWarnCountForUser(AUserInAServer aUserInAServer);
    List<Warning> getActiveWarnsForUser(AUserInAServer aUserInAServer);
    Optional<Warning> findByIdOptional(Long id, Long serverId);
    Warning findById(Long id, Long serverId);
    List<Warning> getWarningsViaId(List<Long> warnIds, Long serverId);
    void deleteWarning(Warning warn);
    Optional<Warning> findWarnByInfraction(Long infractionId);
}

package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.Warning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarnRepository extends JpaRepository<Warning, ServerSpecificId> {
    List<Warning> findAllByWarnedUser_ServerReferenceAndDecayedFalseAndWarnDateLessThan(AServer server, Instant cutOffDate);
    List<Warning> findAllByWarnedUser_ServerReferenceAndDecayedFalseAndWarnDateGreaterThan(AServer server, Instant cutOffDate);
    List<Warning> findAllByWarnedUserAndDecayedFalseAndWarnDateGreaterThan(AUserInAServer user, Instant cutOffDate);

    List<Warning> findAllByWarnedUser_ServerReference(AServer server);

    Long countByWarnedUser(AUserInAServer aUserInAServer);

    Long countByWarnedUserAndDecayedFalse(AUserInAServer aUserInAServer);
    List<Warning> findByWarnedUserAndDecayedFalse(AUserInAServer aUserInAServer);

    List<Warning> findByWarnedUser(AUserInAServer aUserInAServer);

    Optional<Warning> findByWarnId_IdAndWarnId_ServerId(Long warnId, Long serverId);
    Optional<Warning> findByInfraction_Id(Long infractionId);
}

package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarnRepository extends JpaRepository<Warning, Long> {
    List<Warning> findAllByWarnedUser_ServerReferenceAndDecayedFalseAndWarnDateLessThan(AServer server, Instant cutOffDate);

    List<Warning> findAllByWarnedUser_ServerReference(AServer server);

    Long countByWarnedUser(AUserInAServer aUserInAServer);

    Long countByWarnedUserAndDecayedFalse(AUserInAServer aUserInAServer);

    List<Warning> findByWarnedUser(AUserInAServer aUserInAServer);

    @NotNull
    @Override
    Optional<Warning> findById(@NonNull Long aLong);

    @NotNull
    Optional<Warning> findByWarnId_IdAndWarnId_ServerId(Long warnId, Long serverId);
}

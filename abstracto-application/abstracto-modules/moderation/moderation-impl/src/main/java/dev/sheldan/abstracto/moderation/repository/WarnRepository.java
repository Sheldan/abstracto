package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface WarnRepository extends JpaRepository<Warning, Long> {
    List<Warning> findAllByWarnedUser_ServerReferenceAndDecayedFalseAndWarnDateLessThan(AServer server, Instant cutOffDate);
}

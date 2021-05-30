package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.ReactionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface ReactionReportRepository extends JpaRepository<ReactionReport, Long> {

    Optional<ReactionReport> findByReportedUserAndCreatedLessThan(AUserInAServer aUserInAServer, Instant maxCreated);
}

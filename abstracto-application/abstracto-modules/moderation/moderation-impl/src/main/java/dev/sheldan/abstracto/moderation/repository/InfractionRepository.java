package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfractionRepository extends JpaRepository<Infraction, Long> {
    List<Infraction> findByUserAndDecayedFalse(AUserInAServer aUserInAServer);
    List<Infraction> findByUserOrderByCreated(AUserInAServer aUserInAServer);
    List<Infraction> findByServerOrderByCreated(AServer server);
}

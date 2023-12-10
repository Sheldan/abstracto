package dev.sheldan.abstracto.giveaway.repository;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.giveaway.model.database.Giveaway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiveawayRepository extends JpaRepository<Giveaway, ServerSpecificId> {
}

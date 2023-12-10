package dev.sheldan.abstracto.giveaway.repository;

import dev.sheldan.abstracto.giveaway.model.database.GiveawayParticipant;
import dev.sheldan.abstracto.giveaway.model.database.embed.GiveawayParticipationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiveawayParticipantRepository extends JpaRepository<GiveawayParticipant, GiveawayParticipationId> {
}

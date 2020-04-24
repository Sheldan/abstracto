package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MuteRepository extends JpaRepository<Mute, Long> {
    boolean existsByMutedUserAndMuteEndedFalse(AUserInAServer userInAServer);
}

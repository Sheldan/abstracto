package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.Mute;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MuteRepository extends JpaRepository<Mute, ServerSpecificId> {
    boolean existsByMutedUserAndMuteEndedFalse(AUserInAServer userInAServer);

    Mute findTopByMutedUserAndMuteEndedFalse(AUserInAServer userInAServer);

    List<Mute> findAllByMutedUserAndMuteEndedFalseOrderByMuteId_IdDesc(AUserInAServer aUserInAServer);

    @NotNull
    Optional<Mute> findByMuteId_IdAndMuteId_ServerId(Long muteId, Long serverId);
}

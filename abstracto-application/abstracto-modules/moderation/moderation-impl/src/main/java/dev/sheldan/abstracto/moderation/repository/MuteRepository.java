package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface MuteRepository extends JpaRepository<Mute, ServerSpecificId> {
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsByMutedUserAndMuteEndedFalse(AUserInAServer userInAServer);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Mute findTopByMutedUserAndMuteEndedFalse(AUserInAServer userInAServer);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<Mute> findAllByMutedUserAndMuteEndedFalseOrderByMuteId_IdDesc(AUserInAServer aUserInAServer);

    @NotNull
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Optional<Mute> findByMuteId_IdAndMuteId_ServerId(Long muteId, Long serverId);
}

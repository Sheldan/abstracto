package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmoteRepository extends JpaRepository<AEmote, Integer> {

    AEmote findAEmoteByNameAndServerRef(String name, AServer server);

    boolean existsByNameAndServerRef(String name, AServer server);

    Optional<AEmote> findByEmoteId(Long emoteId);

    boolean existsByEmoteId(Long emoteId);

    boolean existsByEmoteIdAndServerRef(Long emoteId, AServer server);

    @Override
    Optional<AEmote> findById(@NonNull Integer aLong);
}

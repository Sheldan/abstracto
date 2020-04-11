package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmoteRepository extends JpaRepository<AEmote, Long> {
    AEmote findAEmoteByNameAndServerRef(String name, AServer server);
    boolean existsByNameAndServerRef(String name, AServer server);
}
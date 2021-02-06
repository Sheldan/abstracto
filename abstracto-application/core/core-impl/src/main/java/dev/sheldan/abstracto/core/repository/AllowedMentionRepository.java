package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AllowedMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AllowedMentionRepository extends JpaRepository<AllowedMention, Long> {
    Optional<AllowedMention> findByServer(AServer server);

}

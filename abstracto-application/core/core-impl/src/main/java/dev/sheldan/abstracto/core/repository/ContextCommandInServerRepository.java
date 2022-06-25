package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ContextCommand;
import dev.sheldan.abstracto.core.models.database.ContextCommandInServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContextCommandInServerRepository extends JpaRepository<ContextCommandInServer, Long> {
    Optional<ContextCommandInServer> findByCommandReferenceAndServerReference(ContextCommand contextCommand, AServer server);
}

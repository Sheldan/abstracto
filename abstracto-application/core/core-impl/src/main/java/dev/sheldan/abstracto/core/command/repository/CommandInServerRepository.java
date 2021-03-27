package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CommandInServerRepository extends JpaRepository<ACommandInAServer, Long> {

    Optional<ACommandInAServer> findByServerReferenceAndCommandReference(AServer server, ACommand command);

    ACommandInAServer findByServerReference_IdAndCommandReference(Long serverId, ACommand command);
}

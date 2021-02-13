package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.models.database.ACommandInAServer;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CommandInServerRepository extends JpaRepository<ACommandInAServer, Long> {

    ACommandInAServer findByServerReferenceAndCommandReference(AServer server, ACommand command);

    ACommandInAServer findByServerReference_IdAndCommandReference(Long serverId, ACommand command);
}

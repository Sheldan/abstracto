package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.models.database.ACommandInAServer;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;

@Repository
public interface CommandInServerRepository extends JpaRepository<ACommandInAServer, Long> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    ACommandInAServer findByServerReferenceAndCommandReference(AServer server, ACommand command);
}

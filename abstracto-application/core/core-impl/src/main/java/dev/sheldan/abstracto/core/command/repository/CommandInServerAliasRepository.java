package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.model.database.ACommandInServerAlias;
import dev.sheldan.abstracto.core.command.model.database.CommandInServerAliasId;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommandInServerAliasRepository extends JpaRepository<ACommandInServerAlias, CommandInServerAliasId> {
    List<ACommandInServerAlias> findByCommandInAServer_ServerReference(AServer server);
    Optional<ACommandInServerAlias> findByCommandInAServer_ServerReferenceAndAliasId_NameEqualsIgnoreCase(AServer server, String name);
    List<ACommandInServerAlias> findByCommandInAServer_ServerReferenceAndCommandInAServer_CommandReference_NameEqualsIgnoreCase(AServer server, String name);
    boolean existsByCommandInAServer_ServerReferenceAndAliasId_NameEqualsIgnoreCase(AServer server, String name);
}

package dev.sheldan.abstracto.customcommand.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomCommandRepository extends JpaRepository<CustomCommand, Long> {
    Optional<CustomCommand> getByNameAndServer(String name, AServer server);
}

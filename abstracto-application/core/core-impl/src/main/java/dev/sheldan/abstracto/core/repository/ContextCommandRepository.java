package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.ContextCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContextCommandRepository extends JpaRepository<ContextCommand, Long> {
    Optional<ContextCommand> findByCommandName(String commandName);
}

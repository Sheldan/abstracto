package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommandRepository extends JpaRepository<ACommand, Long> {

    Optional<ACommand> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}

package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandRepository extends JpaRepository<ACommand, Long> {
    ACommand findByName(String name);
}

package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.models.ACommand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandRepository extends JpaRepository<ACommand, Long> {
    ACommand findByName(String name);
}

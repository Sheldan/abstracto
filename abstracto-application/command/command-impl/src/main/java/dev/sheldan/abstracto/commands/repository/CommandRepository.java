package dev.sheldan.abstracto.commands.repository;

import dev.sheldan.abstracto.command.models.ACommand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandRepository extends JpaRepository<ACommand, Long> {
    ACommand findByName(String name);
}

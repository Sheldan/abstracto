package dev.sheldan.abstracto.commands.repository;

import dev.sheldan.abstracto.command.models.AModule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<AModule, Long> {
    AModule findByName(String name);
}

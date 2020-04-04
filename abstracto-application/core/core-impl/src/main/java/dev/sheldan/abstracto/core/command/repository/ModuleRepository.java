package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.models.AModule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<AModule, Long> {
    AModule findByName(String name);
}

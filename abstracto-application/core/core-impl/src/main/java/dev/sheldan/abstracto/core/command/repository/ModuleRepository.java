package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.models.database.AModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;

public interface ModuleRepository extends JpaRepository<AModule, Long> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    AModule findByName(String name);
}

package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.ADefaultConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DefaultConfigRepository extends JpaRepository<ADefaultConfig, Long> {
    ADefaultConfig findByName(String name);
    boolean existsByName(String name);
}

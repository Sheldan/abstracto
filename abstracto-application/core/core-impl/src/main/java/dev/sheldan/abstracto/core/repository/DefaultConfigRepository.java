package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.ADefaultConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;

@Repository
public interface DefaultConfigRepository extends JpaRepository<ADefaultConfig, Long> {
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    ADefaultConfig findByName(String name);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsByName(String name);
}

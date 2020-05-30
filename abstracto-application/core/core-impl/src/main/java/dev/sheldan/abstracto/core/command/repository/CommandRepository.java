package dev.sheldan.abstracto.core.command.repository;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;

@Repository
public interface CommandRepository extends JpaRepository<ACommand, Long> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    ACommand findByName(String name);

    boolean existsByName(String name);
}

package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerRepository extends JpaRepository<AServer, Long> {
    List<AServer> findAll();
}

package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponentPayloadRepository extends JpaRepository<ComponentPayload, String> {
    List<ComponentPayload> findByServerAndOrigin(AServer server, String buttonOrigin);
}

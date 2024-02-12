package dev.sheldan.abstracto.stickyroles.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.stickyroles.model.database.StickyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StickyRoleRepository extends JpaRepository<StickyRole, Long> {
    List<StickyRole> findStickyRoleByServer(AServer server);
}

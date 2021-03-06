package dev.sheldan.abstracto.modmail.repository;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.modmail.model.database.ModMailRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository to manage the stored {@link ModMailRole} instances
 */
@Repository
public interface ModMailRoleRepository extends JpaRepository<ModMailRole, Long> {
    boolean existsByServerAndRole(AServer server, ARole role);

    void deleteByServerAndRole(AServer server, ARole role);

    List<ModMailRole> findByServer(AServer server);
}

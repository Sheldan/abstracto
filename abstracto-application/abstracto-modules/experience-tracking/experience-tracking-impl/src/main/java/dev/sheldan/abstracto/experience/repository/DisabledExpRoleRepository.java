package dev.sheldan.abstracto.experience.repository;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.ADisabledExpRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository to manage the access to the table managed by {@link ADisabledExpRole}
 */
@Repository
public interface DisabledExpRoleRepository extends JpaRepository<ADisabledExpRole, Long> {
    boolean existsByRole(ARole role);

    ADisabledExpRole findByRole(ARole role);

    void deleteByRole(ARole role);

    List<ADisabledExpRole> getByRole_Server(AServer server);
}

package dev.sheldan.abstracto.experience.repository;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.ADisabledExpRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository to manage the access to the table managed by {@link ADisabledExpRole disabledRole}
 */
@Repository
public interface DisabledExpRoleRepository extends JpaRepository<ADisabledExpRole, Long> {

    /**
     * Determines if there exists a {@link ADisabledExpRole disabledExpRole} which is defined by the given {@link ARole role}
     * @param role The {@link ARole role} to search for
     * @return Whether or not a role {@link ADisabledExpRole disabledExpRole} for this {@link ARole role} eixsts
     */
    boolean existsByRole(ARole role);

    /**
     * Deletes the {@link ADisabledExpRole disabledExpRole} defined for this {@link ARole role}, if there is any.
     * @param role The {@link ARole} to delete the {@link ADisabledExpRole disabledExpRole} for
     */
    void deleteByRole(ARole role);

    /**
     * Returns all {@link ADisabledExpRole disabledExpRoles} for the given {@link AServer server}
     * @param server The {@link AServer server} to retrieve the {@link ADisabledExpRole disabledExpRoles} for
     * @return The list of {@link ADisabledExpRole disabledExpRoles} found, can be empty
     */
    List<ADisabledExpRole> getByRole_Server(AServer server);
}

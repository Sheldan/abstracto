package dev.sheldan.abstracto.experience.repository;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository to manage the access to the table managed by {@link AExperienceRole experienceRole}
 */
@Repository
public interface ExperienceRoleRepository extends JpaRepository<AExperienceRole, Long> {
    /**
     * Finds the {@link AExperienceRole experienceRole} of the given {@link AServer server} and {@link ARole role}
     * @param role The {@link ARole role} to filter for
     * @return The {@link AExperienceRole experienceRole} found or null if the query did not return any results
     */
    Optional<AExperienceRole> findByRole(ARole role);

    /**
     * Finds a list of {@link AExperienceRole experienceRoles} (if there are multiple ones, because of misconfiguration) of the given
     * {@link AExperienceLevel experienceLevel} and {@link AServer server}
     * @param level The {@link AExperienceLevel experienceLevel} to search for
     * @param server The {@link AServer server} to search for
     * @return The list of {@link AExperienceRole experienceRole} found by the given parameters
     */
    List<AExperienceRole> findByLevelAndRoleServer(AExperienceLevel level, AServer server);

    /**
     * Finds all {@link AExperienceRole experienceRoles} of the given {@link AServer server}
     * @param server The {@link AServer server} to load the list of {@link AExperienceRole experienceRoles} for
     * @return A list of {@link AExperienceRole experienceRoles} configured to be used on the given {@link AServer server}
     */
    List<AExperienceRole> findByRoleServer(AServer server);

}

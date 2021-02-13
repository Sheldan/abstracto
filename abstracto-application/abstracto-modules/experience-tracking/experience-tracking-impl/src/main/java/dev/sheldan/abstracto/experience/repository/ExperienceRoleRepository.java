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
 * Repository to manage the access to the table managed by {@link AExperienceRole}
 */
@Repository
public interface ExperienceRoleRepository extends JpaRepository<AExperienceRole, Long> {
    /**
     * Finds the {@link AExperienceRole} of the given {@link AServer} and {@link ARole}
     * @param role The {@link ARole} to filter for
     * @return The {@link AExperienceRole} found or null if the query did not return any results
     */
    Optional<AExperienceRole> findByRole(ARole role);

    /**
     * Finds a list of {@link AExperienceRole} (if there are multiple ones, because of misconfiguration) of the given
     * {@link AExperienceLevel} and {@link AServer}
     * @param level The {@link AExperienceLevel} to search for
     * @param server The {@link AServer} to search for
     * @return The list of {@link AExperienceRole} found by the given parameters
     */
    List<AExperienceRole> findByLevelAndRoleServer(AExperienceLevel level, AServer server);

    /**
     * Finds all {@link AExperienceRole} of the given {@link AServer}
     * @param server The {@link AServer} to load the list of {@link AExperienceRole} for
     * @return A list of {@link AExperienceRole} configured to be used on the given {@link AServer}
     */
    List<AExperienceRole> findByRoleServer(AServer server);

    @NotNull
    @Override
    Optional<AExperienceRole> findById(@NonNull Long aLong);
}

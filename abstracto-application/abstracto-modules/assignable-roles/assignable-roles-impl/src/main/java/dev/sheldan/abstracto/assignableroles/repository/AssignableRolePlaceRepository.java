package dev.sheldan.abstracto.assignableroles.repository;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository to manage the access to the table managed by {@link AssignableRolePlace place}
 */
@Repository
public interface AssignableRolePlaceRepository extends JpaRepository<AssignableRolePlace, Long> {

    /**
     * Whether or not an {@link AssignableRolePlace place} exists in a {@link AServer server} with the given key
     * @param server The {@link AServer server} to search in
     * @param key The key to search for
     * @return Whether or not a {@link AssignableRolePlace place} exists in the {@link AServer server} with the key
     */
    boolean existsByServerAndKey(AServer server, String key);

    /**
     * Finds an {@link AssignableRolePlace place} in a {@link AServer server} with the given key,
     * returns an empty {@link Optional optional} otherwise
     * @param server The {@link AServer server} to search in
     * @param key The key to search for
     * @return An {@link Optional optional} containing the {@link AssignableRolePlace place} if it exists, empty otherwise.
     */
    Optional<AssignableRolePlace> findByServerAndKey(AServer server, String key);

    /**
     * Finds all {@link AssignableRolePlace places} in the given {@link AServer server}
     * @param server The {@link AServer server} to retrieve {@link AssignableRolePlace places} for
     * @return A list of {@link AssignableRolePlace places} which were found in the given {@link AServer server}
     */
    List<AssignableRolePlace> findByServer(AServer server);

}

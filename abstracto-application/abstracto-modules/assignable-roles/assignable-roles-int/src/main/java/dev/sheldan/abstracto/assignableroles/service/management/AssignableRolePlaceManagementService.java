package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.List;
import java.util.Optional;

/**
 * Management service for {@link AssignableRolePlace place} table
 */
public interface AssignableRolePlaceManagementService {

    /**
     * Creates an {@link AssignableRolePlace place} with the given attributes
     * @param name The key of the {@link AssignableRolePlace place} to identify it with
     * @param channel The {@link AChannel channel} in which the {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost posts}
     *                should be created in
     * @param text The text which should be shown in the description
     * @return The {@link AssignableRolePlace place} which was created
     */
    AssignableRolePlace createPlace(String name, AChannel channel, String text);

    /**
     * Whether or not a place with the key exists in the given {@link AServer server}
     * @param server The {@link AServer server} in which it should be searched
     * @param name The key of an {@link AssignableRolePlace place} which should be searched
     * @return Whether or not an {@link AssignableRolePlace place} with the key exists in the {@link AServer server}
     */
    boolean doesPlaceExist(AServer server, String name);

    /**
     * Retrieves an {@link AssignableRolePlace place} identified by the given key in the {@link AServer server}
     * @param server The {@link AServer server} to search in
     * @param name The key of the {@link AssignableRolePlace place} to search for
     * @throws dev.sheldan.abstracto.assignableroles.exceptions.AssignableRolePlaceNotFoundException if not found
     * @return Returns an instance of {@link AssignableRolePlace place}, if it was found
     */
    AssignableRolePlace findByServerAndKey(AServer server, String name);

    /**
     * Retrieves an {@link AssignableRolePlace place} via its ID in an {@link Optional optional}
     * @param id The ID to search for
     * @return Returns an {@link Optional optional} with an instance of {@link AssignableRolePlace place}, if it was found
     * and empty otherwise
     */
    Optional<AssignableRolePlace> findByPlaceIdOptional(Long id);

    /**
     * Retrieves an {@link AssignableRolePlace place} via its ID
     * @param id The ID to search for
     * @throws dev.sheldan.abstracto.assignableroles.exceptions.AssignableRolePlaceNotFoundException if not found
     * @return Returns an {@link AssignableRolePlace place} if one was found with this ID
     */
    AssignableRolePlace findByPlaceId(Long id);

    /**
     * Changes the {@link AChannel channel} in which a {@link AssignableRolePlace place} should be posted towards
     * when being setup.
     * @param name The key of the {@link AssignableRolePlace place} to move
     * @param newChannel The new {@link AChannel channel} where the place should be moved to, used to determine in which {@link AServer server}
     *                   the {@link AssignableRolePlace place} is
     */
    void moveAssignableRolePlace(String name, AChannel newChannel);

    /**
     * Changes the {@link AssignableRolePlace#text description} of an assignable role place
     * @param server The {@link AServer server} in which to look for the place
     * @param name The key of the {@link AssignableRolePlace place} to change the descripiont for
     * @param newDescription The description value which should be used from now on
     */
    void changeAssignableRolePlaceDescription(AServer server, String name, String newDescription);

    /**
     * Deletes the {@link AssignableRolePlace place}
     * @param place The {@link AssignableRolePlace place} to remove
     */
    void deleteAssignablePlace(AssignableRolePlace place);

    /**
     * Retrieves all {@link AssignableRolePlace assignableRolePlaces} for the given {@link AServer server}
     * @param server The {@link AServer server} for  whom the places should be retrieved for
     * @return All {@link AssignableRolePlace assignableRolePlaces} in the given {@link AServer server}
     */
    List<AssignableRolePlace> findAllByServer(AServer server);

}

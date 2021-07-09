package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import net.dv8tion.jda.api.entities.Role;

/**
 * Management service for the table of {@link AssignableRole assignableRoles}
 */
public interface AssignableRoleManagementService {
    /**
     * Adds the given {@link ARole role} to the {@link AssignableRolePlace place} to be identified with the given {@link AEmote emote}
     * and displayed with the given description. An optional {@link AssignableRolePlacePost post} can be provided, if the
     * place has already been setup.
     * @param emote The {@link AEmote emote} which is used as an reaction on the {@link AssignableRolePlacePost post}
     * @param role The {@link ARole role} which should be given to the {@link dev.sheldan.abstracto.core.models.database.AUserInAServer user} placing a reaction
     * @param description The description which should be displayed in the field for the given {@link ARole role}
     * @return The created instance of the {@link AssignableRole assignableRole} according to the given parameters
     */
    AssignableRole addRoleToPlace(FullEmote emote, Role role, String description, AssignableRolePlace place, ComponentPayload componentPayload);

    /**
     * Finds the {@link AssignableRole} given by the ID and returns it if found. Throws an exception otherwise.
     * @param assignableRoleId The ID Of the {@link AssignableRole assignableRole} to find
     * @return An instance of {@link AssignableRole assignableRole} if it exists for the given ID
     */
    AssignableRole getByAssignableRoleId(Long assignableRoleId);
    void deleteAssignableRole(AssignableRole assignableRole);

    /**
     * Returns the respective {@link AssignableRole assignableRole} for the {@link CachedEmote emote} which is part of the
     * {@link AssignableRolePlace place}. It will throw an exception, if the {@link CachedEmote emote} is not used.
     * @param cachedEmote The {@link CachedEmote emote} which should be used to identify the {@link AssignableRole role}
     * @param assignableRolePlace The {@link AssignableRolePlace place} from which the {@link AssignableRole role} should be retrieved for
     * @return An instance of {@link AssignableRole role} which was in the place and identified by the emote
     */
}

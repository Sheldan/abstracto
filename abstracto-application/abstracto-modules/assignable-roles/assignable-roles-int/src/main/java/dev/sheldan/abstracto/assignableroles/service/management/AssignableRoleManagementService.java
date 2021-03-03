package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.ARole;

/**
 * Management service for the table of {@link AssignableRole assignableRoles}
 */
public interface AssignableRoleManagementService {
    /**
     * Adds the given {@link ARole role} to the {@link AssignableRolePlace place} to be identified with the given {@link AEmote emote}
     * and displayed with the given description. An optional {@link AssignableRolePlacePost post} can be provided, if the
     * place has already been setup.
     * @param place The {@link AssignableRolePlace place} to add the the {@link ARole role} to
     * @param emote The {@link AEmote emote} which is used as an reaction on the {@link AssignableRolePlacePost post}
     * @param role The {@link ARole role} which should be given to the {@link dev.sheldan.abstracto.core.models.database.AUserInAServer user} placing a reaction
     * @param description The description which should be displayed in the field for the given {@link ARole role}
     * @param post If a {@link AssignableRolePlacePost post} already exists, it can be provided to link the newly created {@link AssignableRole directly}
     * @return The created instance of the {@link AssignableRole assignableRole} according to the given parameters
     */
    AssignableRole addRoleToPlace(AssignableRolePlace place, AEmote emote, ARole role, String description, AssignableRolePlacePost post);

    /**
     * Adds the {@link ARole role} (identified by its ID) to the {@link AssignableRolePlace place} (identified by its ID),
     * which in turn is identified by the given {@link AEmote emote} (identified by its ID) and displayed with the given description.
     * An optional {@link AssignableRolePlacePost post} can be provided (identified by the ID of the {@link net.dv8tion.jda.api.entities.Message}), if
     * it already exists
     * @param placeId The ID of the {@link AssignableRolePlace} to add an {@link AssignableRole assignableRole} to
     * @param emoteId The ID of the {@link AEmote emote} which should be used as an reaction on the {@link AssignableRolePlacePost post}
     * @param roleId The ID of the {@link ARole role} which should be given to the {@link dev.sheldan.abstracto.core.models.database.AUserInAServer user} placing a reaction
     * @param description The description which should be displayed in the field for the given {@link ARole role}
     * @param messageId If provided, this message ID will be used to identify the {@link AssignableRolePlacePost post} which already exists
     * @return The created instance of the {@link AssignableRole assignableRole} according to the given parameters
     */
    AssignableRole addRoleToPlace(Long placeId, Integer emoteId, Long roleId, String description,  Long messageId);

    /**
     * Adds the {@link ARole role} (identified by its ID) to the {@link AssignableRolePlace place} (identified by its ID),
     * which in turn is identified by the given {@link AEmote emote} (identified by its ID) and displayed with the given description.
     * @param placeId The ID of the {@link AssignableRolePlace} to add an {@link AssignableRole assignableRole} to
     * @param emoteId The ID of the {@link AEmote emote} which should be used as an reaction on the {@link AssignableRolePlacePost post}
     * @param roleId The ID of the {@link ARole role} which should be given to the {@link dev.sheldan.abstracto.core.models.database.AUserInAServer user} placing a reaction
     * @param description The description which should be displayed in the field for the given {@link ARole role}
     * @return The created instance of the {@link AssignableRole assignableRole} according to the given parameters
     */
    AssignableRole addRoleToPlace(Long placeId, Integer emoteId, Long roleId, String description);

    /**
     * Finds the {@link AssignableRole} given by the ID and returns it if found. Throws an exception otherwise.
     * @param assignableRoleId The ID Of the {@link AssignableRole assignableRole} to find
     * @return An instance of {@link AssignableRole assignableRole} if it exists for the given ID
     */
    AssignableRole getByAssignableRoleId(Long assignableRoleId);

    /**
     * Returns the respective {@link AssignableRole assignableRole} for the {@link CachedEmote emote} which is part of the
     * {@link AssignableRolePlace place}. It will throw an exception, if the {@link CachedEmote emote} is not used.
     * @param cachedEmote
     * @param assignableRolePlace
     * @return
     */
    AssignableRole getRoleForReactionEmote(CachedEmote cachedEmote, AssignableRolePlace assignableRolePlace);
}

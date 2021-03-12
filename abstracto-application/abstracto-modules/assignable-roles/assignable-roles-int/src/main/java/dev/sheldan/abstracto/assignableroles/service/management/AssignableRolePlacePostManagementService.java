package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.exception.AssignableRolePlacePostNotFoundException;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlacePost;

import java.util.Optional;

/**
 * Management service for {@link AssignableRolePlacePost post} table
 */
public interface AssignableRolePlacePostManagementService {
    /**
     * Finds a {@link AssignableRolePlacePost post} via the ID of the {@link net.dv8tion.jda.api.entities.Message} it was
     * posted
     * @param messageId The ID of the {@link net.dv8tion.jda.api.entities.Message} in which the
     * @return An {@link Optional optional} containing the {@link AssignableRolePlacePost post}, if one was found, empty otherwise
     */
    Optional<AssignableRolePlacePost> findByMessageIdOptional(Long messageId);

    /**
     * Finds a {@link AssignableRolePlacePost post} via the ID of the {@link net.dv8tion.jda.api.entities.Message} it was
     * posted
     * @param messageId The ID of the {@link net.dv8tion.jda.api.entities.Message} in which the
     * @throws AssignableRolePlacePostNotFoundException if it was not found
     * @return The {@link AssignableRolePlacePost post} if one existed with this ID
     */
    AssignableRolePlacePost findByMessageId(Long messageId);

    /**
     * Creates an {@link AssignableRolePlacePost post} for the given {@link AssignableRolePlace place} in the given message ID
     * @param updatedPlace The {@link AssignableRolePlace place} this post should be part of
     * @param messageId The ID of the message in which the post exists
     * @return The {@link AssignableRolePlacePost post} which is found in the given {@link AssignableRolePlace place} of the {@link net.dv8tion.jda.api.entities.Message message} ID
     */
    AssignableRolePlacePost createAssignableRolePlacePost(AssignableRolePlace updatedPlace, Long messageId);
}

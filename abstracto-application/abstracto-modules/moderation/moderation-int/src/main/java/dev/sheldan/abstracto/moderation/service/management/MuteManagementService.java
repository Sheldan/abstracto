package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.Mute;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Responsible for creating/updating/retrieving mutes in the database.
 */
public interface MuteManagementService {
    /**
     * Creates a mute object with the given parameters. The only parameter set by this method is that, the mute is not ended yet.
     * @param mutedUser The member which is being muted
     * @param mutingUser The member which mutes
     * @param reason The reason why this user is getting muted
     * @param unMuteDate The date at which the mute should end
     * @param muteMessage The message containing the command which caused the mute
     * @param triggerKey The key of the trigger in quartz, if any
     * @param muteId The id of the mute to create
     * @param infractionId The ID of the matching infraction
     * @return The created mute object containing the mute ID
     */
    Mute createMute(AUserInAServer mutedUser, AUserInAServer mutingUser, String reason, Instant unMuteDate, AServerAChannelMessage muteMessage, String triggerKey, Long muteId, Long infractionId);

    /**
     * Finds the mute from the database by the given ID.
     * @param muteId The ID of the mute to search for
     * @param serverId The ID of the server to retrieve the {@link Mute mute} in
     * @return An optional containing a {@link Mute} if the ID exists, and null otherwise
     */
    Optional<Mute> findMuteOptional(Long muteId, Long serverId);
    Mute findMute(Long muteId, Long serverId);

    /**
     * Saves the given mute to the database.
     * @param mute The {@link Mute} to save
     * @return The (maybe) updated {@link Mute} object
     */
    Mute saveMute(Mute mute);

    /**
     * Returns if the given {@link AUserInAServer} has an active mute which has not yet been ended yet.
     * @param userInAServer The {@link AUserInAServer} to check for
     * @return Whether or not the userInAServer has an active mute
     */
    boolean hasActiveMute(AUserInAServer userInAServer);

    /**
     * Returns if the given {@link AUserInAServer} has an active mute which has not yet been ended yet.
     * @param member The {@link Member} to check for
     * @return Whether or not the userInAServer has an active mute
     */
    boolean hasActiveMute(Member member);

    /**
     * Returns any active {@link Mute} of this {@link AUserInAServer} in the database
     * @param userInAServer The {@link AUserInAServer} to search a mute for
     * @return The found {@link Mute}, and null if none was found
     */
    Mute getAMuteOf(AUserInAServer userInAServer);
    Optional<Mute> getAMuteOfOptional(AUserInAServer userInAServer);

    /**
     * Returns any active {@link Mute} of this {@link Member} in the database
     * @param member The {@link Member} to search a mute for
     * @return The found {@link Mute}, and null if none was found
     */
    Mute getAMuteOf(Member member);
    Optional<Mute> getAMuteOfOptional(Member member);

    /**
     * Retrieves all active mutes of the given {@link AUserInAServer} in a collection
     * @param aUserInAServer The {@link AUserInAServer} to search the active mutes for
     * @return A collection of {@link Mute} objects of the user which are active
     */
    List<Mute> getAllActiveMutesOf(AUserInAServer aUserInAServer);

    /**
     * Retrieves all mutes of the given {@link AUserInAServer} in a collection
     * @param aUserInAServer The {@link AUserInAServer} to search the mutes for
     * @return A collection of {@link Mute} objects of the user
     */
    List<Mute> getAllMutesOf(AUserInAServer aUserInAServer);

    /**
     * Retrieves all {@link Mute} from the given {@link AServer}
     * @return All found mutes of this server
     */
    List<Mute> getAllMutes(AServer server);
}

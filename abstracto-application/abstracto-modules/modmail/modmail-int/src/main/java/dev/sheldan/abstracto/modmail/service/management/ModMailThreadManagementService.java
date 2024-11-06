package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.database.ModMailThreadState;

import java.util.List;
import java.util.Optional;

/**
 * Management service to create/retrieve/modify instances of {@link ModMailThread}
 */

public interface ModMailThreadManagementService {

    /**
     * Retrieves a {@link ModMailThread} found in the message channel given by the ID of the channel
     * @param channelId The id of the channel to retrieve the {@link ModMailThread} for
     * @throws ChannelNotFoundException if an appropriate {@link AChannel} was not found
     * @return The instance of {@link ModMailThread} if it exists, null if none was found
     */
    ModMailThread getByChannelId(Long channelId);

    /**
     * Retrieves the {@link ModMailThread} by the given ID in an optional, if it exists, and an {@literal Optional#empty()} otherwise
     * @param modMailThreadId The ID of the mod mail to search for
     * @return An {@link Optional} containing the mod mail thread or empty
     */
    Optional<ModMailThread> getByIdOptional(Long modMailThreadId);

    /**
     * Retrieves the {@link ModMailThread} by the given ID in an optional, if it exists, and an {@literal Optional#empty()} otherwise
     * @param modMailThreadId The ID of the mod mail to search for
     * @return An {@link Optional} containing the mod mail thread or empty
     */
    ModMailThread getById(Long modMailThreadId);

    /**
     * Retrieves a {@link ModMailThread} found in the message channel given by the
     * {@link AChannel} object
     * @param channel The {@link AChannel} object to search a mod mail thread for
     * @return The found mod mail thread, or null if none was found
     */
    ModMailThread getByChannel(AChannel channel);
    Optional<ModMailThread> getByChannelOptional(AChannel channel);
    Optional<ModMailThread> getByChannelIdOptional(Long channelId);

    /**
     * Searches for mod mail threads with the appropriate staten which concern the given {@link AUserInAServer}
     * @param userInAServer The {@link AUserInAServer} to search mod mail threads for
     * @param state The {@link ModMailThreadState} to be used as a criteria
     * @return A list of {@link ModMailThread} which were found by the given criteria
     */
    List<ModMailThread> getThreadByUserAndState(AUserInAServer userInAServer, ModMailThreadState state);

    /**
     * Retrieves the *only* open mod mail thread for the given {@link AUserInAServer}, and null if none is in the state open
     * @param userInAServer The {@link AUserInAServer} to search an open {@link ModMailThread} for
     * @return The found open {@link ModMailThread}, or null if none is found
     */
    ModMailThread getOpenModMailThreadForUser(AUserInAServer userInAServer);

    /**
     * Returns whether or not the {@link AUserInAServer} has a {@link ModMailThread} in a state which is not CLOSED
     * @param userInAServer The {@link AUserInAServer} to check for
     * @return Boolean whether or not the {@link AUserInAServer} has an open thread
     */
    boolean hasOpenModMailThreadForUser(AUserInAServer userInAServer);

    /**
     * Retrieves all the open {@link ModMailThread} for the {@link AUser}, which means over all the known guilds
     * @param user The {@link AUser} for which the open mod mail threads should be retrieved
     * @return A list of {@link ModMailThread} which contains all the current threads which are not CLOSED
     */
    List<ModMailThread> getOpenModMailThreadsForUser(AUser user);

    /**
     * Returns whether or not the {@link AUser} has a {@link ModMailThread} in a state which is not CLOSED
     * @param user The {@link AUser} to check for open mod mail treads
     * @return Boolean whether or not the {@link AUser} has an open thread
     */
    boolean hasOpenModMailThread(AUser user);

    /**
     * Retrieves all the open {@link ModMailThread} for the {@link AUserInAServer}, which means only in one guild, this
     * should be at most one
     * @param aUserInAServer The {@link AUserInAServer} to retrieve the mod mail threads for
     * @return A list of {@link ModMailThread} which contains all the current mod mail threads for the member, should be at most one
     */
    List<ModMailThread> getModMailThreadForUser(AUserInAServer aUserInAServer);

    /**
     * Retrieves the *latest* {@link ModMailThread} of the {@link AUserInAServer}, which means, the latest thread which is in the state
     * CLOSED, null other wise.
     * @param aUserInAServer The {@link AUserInAServer} to retrieve the latest thread for
     * @return If a thread in state CLOSED exists, the latest one, null other wise
     */
    ModMailThread getLatestModMailThread(AUserInAServer aUserInAServer);

    /**
     * Creates an instance of {@link ModMailThread} with the appropriate parameters and returns the created instance.
     * @param userInAServer The {@link AUserInAServer} for which the thread was created for
     * @param channel An instance of {@link AChannel} in which the conversation with the member is handled
     * @param appeal Whether the modmail thread is for the purpose of an appeal
     * @return The created instance of {@link ModMailThread}
     */
    ModMailThread createModMailThread(AUserInAServer userInAServer, AChannel channel, boolean appeal);

    /**
     * Updates the {@link ModMailThread} with the new state and saves the instance.
     * @param modMailThread The {@link ModMailThread} to change the state for
     * @param newState The new {@link ModMailThreadState} to change the thread to
     */
    void setModMailThreadState(ModMailThread modMailThread, ModMailThreadState newState);

}

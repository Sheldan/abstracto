package dev.sheldan.abstracto.experience.service.management;


import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.models.database.LeaderBoardEntryResult;

import java.util.List;
import java.util.Optional;

/**
 * Service used to manage the record in the {@link AUserExperience} table
 */
public interface UserExperienceManagementService {
    /**
     * Retrieves the {@link AUserExperience} object for the given {@link AUserInAServer}
     * @param aUserInAServer The record in the table referenced by the given {@link AUserInAServer}, if none exists, creates one.
     * @return The {@link AUserExperience} object representing the {@link AUserInAServer}
     */
    AUserExperience findUserInServer(AUserInAServer aUserInAServer);

    Optional<AUserExperience> findByUserInServerIdOptional(Long userInServerId);
    AUserExperience findByUserInServerId(Long userInServerId);

    /**
     * Creates a {@link AUserExperience} object with the default values (0 xp, 0 messages) for the given {@link AUserInAServer} object.
     * @param aUserInAServer The {@link AUserInAServer} to create the {@link AUserExperience} object for.
     * @return The newly created {@link AUserExperience} object
     */
    AUserExperience createUserInServer(AUserInAServer aUserInAServer);

    /**
     * Loads a list of all {@link AUserExperience} objects for a given {@link AServer}.
     * @param server The {@link AServer} to retrieve the list of {@link AUserExperience} for
     * @return A list of {@link AUserExperience} objects associated with the given {@link AServer}
     */
    List<AUserExperience> loadAllUsers(AServer server);

    /**
     * Retrieves a list of {@link AUserExperience} ordered by {@link AUserExperience.experience} and only returns the positions between {@code start} and @{code end}.
     * @param server The {@link AServer} to retrieve the users for
     * @param start The start index in the complete ordered list to return the {@link AUserExperience} elements for
     * @param end The end index for which to return a sublist of {@link AUserExperience} elements for
     * @return A list desc ordered by {@link AUserExperience.experience} only containing the elements between {@code start} and @{code end}
     */
    List<AUserExperience> findLeaderBoardUsersPaginated(AServer server, Integer start, Integer end);

    /**
     * Returns the {@link LeaderBoardEntryResult} of the given {@link AUserExperience}.
     * @param userExperience The {@link AUserExperience} to retrieve the information for
     * @return The {@link LeaderBoardEntryResult} containing the experience, message count and rank of the given userExperience.
     */
    LeaderBoardEntryResult getRankOfUserInServer(AUserExperience userExperience);

    /**
     * Persists the {@link AUserExperience} in the database. Required when creating it
     * @param userExperience The {@link AUserExperience} to persist
     * @return The persisted {@link AUserExperience} instance
     */
    AUserExperience saveUser(AUserExperience userExperience);
}

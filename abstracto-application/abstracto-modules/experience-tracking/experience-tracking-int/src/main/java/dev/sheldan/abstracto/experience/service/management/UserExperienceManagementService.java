package dev.sheldan.abstracto.experience.service.management;


import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LeaderBoardEntryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service used to manage the record in the {@link AUserExperience userExperience} table
 */
public interface UserExperienceManagementService {
    /**
     * Retrieves the {@link AUserExperience userExperience} object for the given {@link AUserInAServer userInAServer}
     * @param aUserInAServer The record in the table referenced by the given {@link AUserInAServer userInAServer}, if none exists, creates one.
     * @return The {@link AUserExperience userExperience} object representing the {@link AUserInAServer userInAServer}
     */
    AUserExperience findUserInServer(AUserInAServer aUserInAServer);
    void removeExperienceRoleFromUsers(AExperienceRole experienceRole);

    /**
     * Retrieves a possible {@link AUserExperience userExperience} for the given ID of the {@link AUserInAServer}.
     * If none is found, returns an empty {@link Optional optional}
     * @param userInServerId The ID of a {@link AUserInAServer} to search for
     * @return An {@link Optional optional} containing a {@link AUserExperience userExperience} object if one is found, none otherwise
     */
    Optional<AUserExperience> findByUserInServerIdOptional(Long userInServerId);

    /**
     * Retrieves the {@link AUserExperience userExperience} object for the given ID of an {@link AUserInAServer userInAServer}
     * @param userInServerId The ID of a {@link AUserInAServer userInAServer} to retrieve the {@link AUserExperience} for.
     * @return The {@link AUserExperience userExperience} object representing the {@link AUserInAServer userInAServer}
     */
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
    Page<AUserExperience> loadAllUsersPaginated(AServer server, Pageable pageable);

    /**
     * Retrieves a list of {@link AUserExperience} ordered by {@link AUserExperience} experience and only returns the positions between {@code start} and @{code end}.
     * @param server The {@link AServer} to retrieve the users for
     * @param page The page to retrieve
     * @param size The size of each page
     * @return A list desc ordered by {@link AUserExperience} experience only containing the elements between {@code start} and @{code end}
     */
    List<AUserExperience> findLeaderBoardUsersPaginated(AServer server, Integer page, Integer size);
    List<LeaderBoardEntryResult> getWindowedLeaderboardEntriesForUser(AUserInAServer aUserInAServer, Integer windowSize);

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

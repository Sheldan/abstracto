package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.experience.model.LeaderBoard;
import dev.sheldan.abstracto.experience.model.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.model.RoleCalculationResult;
import dev.sheldan.abstracto.experience.model.ServerExperience;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Service providing the required mechanisms to provide experience tracking.
 * This includes manipulations on the {@link AUserExperience userExperience} table, container for the runtime experience, synchronizing the
 * user in the guild and retrieving {@link LeaderBoard leaderboard} data.
 */
public interface AUserExperienceService {
    /**
     * Adds the given {@link AUserInAServer userInAServer} to the list of user who gained experience in the current minute.
     * Does not add the user to the list of users, if it is already in there.
     * @param userInAServer The {@link AUserInAServer userInAServer} to be added to the list of users gaining experience
     */
    void addExperience(AUserInAServer userInAServer);

    /**
     * Calculates the appropriate level for the given experience amount according to the given {@link AExperienceLevel levels}
     * configuration.
     * @param levels The list of {@link AExperienceLevel levels} representing the level configuration, this must include the initial level 0
     *               This level will be taken as the initial value, and if no other level qualifies, this will be taken. The levels **must** be ordered.
     * @param experienceCount The amount of experience to calculate the level for
     * @return The appropriate level of the given experience according to the provided {@link AExperienceLevel levels} configuration
     */
    AExperienceLevel calculateLevel(List<AExperienceLevel> levels, Long experienceCount);

    /**
     * Calculates the new level of the provided {@link AUserExperience userExperience} according
     * to the provided list of {@link AExperienceLevel levels} used as level configuration
     * @param userExperience The {@link AUserExperience userExperience} to increase the experience for
     * @param levels The list of {@link AExperienceLevel levels} to be used as level configuration
     * @param experienceCount The amount of experience which will be added
     * @return Whether or not the user changed level
     */
    boolean updateUserLevel(AUserExperience userExperience, List<AExperienceLevel> levels, Long experienceCount);

    /**
     * Iterates through the given list of {@link AServer servers} and increases the experience of the users contained in the
     * {@link ServerExperience serverExperience} object, also increments the level and changes the role if necessary.
     * This uses the respective configurable max/minExp and multiplier for each {@link AServer server} and increases the message count
     * of each user by 1.
     * @param serverExp The list of {@link AServer servers} containing the users which get experience
     * @return A {@link CompletableFuture future} completing when the experience gain was calculated and roles were assigned
     */
    CompletableFuture<Void> handleExperienceGain(List<ServerExperience> serverExp);

    /**
     * Calculates the currently appropriate {@link AExperienceRole} for the given user and updates the role on the
     * {@link net.dv8tion.jda.api.entities.Member} and ond the {@link AUserExperience}. Effectively synchronizes the
     * state in the server and the database.
     * @param userExperience The {@link AUserExperience userExperience} object to recalculate the {@link AExperienceRole experienceRole} for
     * @param roles The list of {@link AExperienceRole roles} used as a role configuration
     * @param currentLevel The current level of the user
     * @return A {@link CompletableFuture future} containing the {@link RoleCalculationResult result} of the role calculation,
     * completing after the role of the {@link net.dv8tion.jda.api.entities.Member} has been updated, if any
     */
    CompletableFuture<RoleCalculationResult> updateUserRole(AUserExperience userExperience, List<AExperienceRole> roles, Integer currentLevel);

    /**
     * Synchronizes the state ({@link AExperienceRole}, {@link net.dv8tion.jda.api.entities.Role})
     * of all the users provided in the {@link AServer} object in the {@link AUserExperience}
     * and on the {@link net.dv8tion.jda.api.entities.Member} according
     * to how much experience the user has. Runs completely in the background.
     * @param server The {@link AServer} to update the users for
     * @return The list of {@link CompletableFuture futures} for each update of the users in the {@link AServer server}
     */
    List<CompletableFuture<RoleCalculationResult>> syncUserRoles(AServer server);

    /**
     * Synchronizes the state ({@link AExperienceRole}, {@link net.dv8tion.jda.api.entities.Role})
     * of all the users provided in the {@link AServer} object in the {@link AUserExperience}
     * and on the {@link net.dv8tion.jda.api.entities.Member} according
     * to how much experience the user has. This method provides feedback back to the user in the provided {@link AChannel channel}
     * while the process is going own.
     * @param server The {@link AServer} to update users for
     * @param channelId The ID of a {@link AChannel channel} in which the {@link dev.sheldan.abstracto.experience.model.template.UserSyncStatusModel statusUpdate}
     *                should be posted to
     * @return A {@link CompletableFuture future} which completes after all the role changes have been completed
     */
    CompletableFuture<Void> syncUserRolesWithFeedback(AServer server, Long channelId);

    /**
     * Recalculates the role of a single user in a server and synchronize the {@link net.dv8tion.jda.api.entities.Role}
     * in the {@link net.dv8tion.jda.api.entities.Guild}
     * @param userExperience The {@link AUserExperience} to synchronize the role for
     * @return A {@link CompletableFuture future} which completes after the roles have been synced for the given {@link AUserInAServer user}
     */
    CompletableFuture<RoleCalculationResult> syncForSingleUser(AUserExperience userExperience);

    /**
     * Loads the desired page of the ordered complete leaderboard from the {@link AServer} and returns the information as a {@link LeaderBoard}
     * @param server The {@link AServer} to retrieve the leaderboard for
     * @param page The desired page on the leaderboard. The pagesize is 10
     * @return The {@link LeaderBoard} containing the {@link LeaderBoardEntry} containing information about the {@link AUserExperience}
     * from the desired page
     */
    LeaderBoard findLeaderBoardData(AServer server, Integer page);

    /**
     * Retrieves the {@link LeaderBoardEntry} from a specific {@link AUserInAServer} containing information about the
     * gained experience
     * @param userInAServer The {@link AUserInAServer} to retrieve the {@link LeaderBoardEntry} for
     * @return The {@link LeaderBoardEntry} containing information about gained experience from the given {@link AUserInAServer}
     * and the rank of the user in the server
     */
    LeaderBoardEntry getRankOfUserInServer(AUserInAServer userInAServer);

    /**
     * Provides a method to execute an action on a list of {@link AUserExperience} and provide feedback in the given {@link AChannel}
     * in the form of {@link dev.sheldan.abstracto.experience.model.template.UserSyncStatusModel} to be rendered with a certain
     * template
     * @param experiences The list of {@link AUserExperience} to be working on
     * @param channel The {@link AChannel} used to provide feedback to the user
     * @param toExecute The {@link Function} which should be executed on each element of the passed list,
     *                  this function needs to take a {@link AUserExperience userExperience} as parameter and returns a {@link CompletableFuture}
     *                  with a {@link RoleCalculationResult} for each of them. These futures are then returned.
     * @return A {@link CompletableFutureList completeFutureList} which represents the individual {@link RoleCalculationResult results} and a primary future, which is completed after all of the individual ones are
     */
    CompletableFutureList<RoleCalculationResult> executeActionOnUserExperiencesWithFeedBack(List<AUserExperience> experiences, AChannel channel, Function<AUserExperience, CompletableFuture<RoleCalculationResult>> toExecute);

    /**
     * Disables the experience gain for a user directly. This sets the `experienceGainDisabled` on the respective {@link AUserExperience} object to true
     * @param userInAServer The {@link AUserInAServer} to disable experience gain for
     */
    void disableExperienceForUser(AUserInAServer userInAServer);

    /**
     * Enables the experience gain for a user directly. This sets the `experienceGainDisabled` on the respective {@link AUserExperience} object to false
     * @param userInAServer The {@link AUserInAServer} to enable experience for
     */
    void enableExperienceForUser(AUserInAServer userInAServer);

    /**
     * Updates the actually stored experience roles in the database
     * @param results The list of {@link RoleCalculationResult} which should be updated in the database
     */
    void syncRolesInStorage(List<RoleCalculationResult> results);
}

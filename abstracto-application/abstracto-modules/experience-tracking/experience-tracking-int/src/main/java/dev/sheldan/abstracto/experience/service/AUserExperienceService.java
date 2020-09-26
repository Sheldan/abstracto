package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.RoleCalculationResult;
import dev.sheldan.abstracto.experience.models.ServerExperience;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;

import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Service providing the required mechanisms to provide experience tracking.
 * This includes manipulations on the {@link AUserExperience} table, container for the runtime experience, synchronizing the
 * user in the guild and retrieving {@link LeaderBoard} data.
 */
public interface AUserExperienceService {
    /**
     * Adds the given {@link AUserInAServer} to the list of user who gained experience in the current minute.
     * Does not add the user to the list of users, if it is already in there.
     * @param userInAServer The {@link AUserInAServer} to be added to the list of users gaining experience
     */
    void addExperience(AUserInAServer userInAServer);

    /**
     * The current representation of the run time experience. Basically a HashMap of minutes to a list of {@link AServer}
     * containing a list of {@link AUserInAServer} which should gain experience in the minute used as key in the HashMap
     * @return
     */
    Map<Long, List<ServerExperience>> getRuntimeExperience();

    /**
     * Calculates the appropriate level of the given {@link AUserExperience} according to the given {@link AExperienceLevel}
     * configuration.
     * @param levels The list of {@link AExperienceLevel} representing the level configuration, this must include the initial level 0
     *               This level will be taken as the initial value, and if no other level qualifies, this will be taken. The levels must be ordered.
     * @param experienceCount
     * @return The appropriate level of {@link AUserExperience} according to the provided {@link AExperienceLevel} configuration
     */
    AExperienceLevel calculateLevel(List<AExperienceLevel> levels, Long experienceCount);

    /**
     * Calculates the new level of the provided {@link AUserExperience} according
     * to the provided list of {@link AExperienceLevel} used as level configuration
     * @param userExperience The {@link AUserExperience} to increase the experience for
     * @param levels The list of {@link AExperienceLevel} to be used as level configuration
     * @param experienceCount
     * @return Whether or not the user changed level
     */
    boolean updateUserLevel(AUserExperience userExperience, List<AExperienceLevel> levels, Long experienceCount);

    /**
     * Iterates through the given list of {@link AServer} and increases the experience of the users contained in the
     * {@link AServer} object, also increments the level and changes the role if necessary.
     * This uses the respective configurable max/minExp and multiplier for each {@link AServer} and increases the message count
     * of each user by 1.
     * @param serverExp The list of {@link AServer} containing the users which get experience
     */
    CompletableFuture<Void> handleExperienceGain(List<ServerExperience> serverExp);

    /**
     * Calculates the currently appropriate {@link AExperienceRole} for the given user and updates the role on the
     * {@link net.dv8tion.jda.api.entities.Member} and ond the {@link AUserExperience}. Effectively synchronizes the
     * state in the server and the database.
     * @param userExperience The {@link AUserExperience} object to recalculate the {@link AExperienceRole} for
     * @param roles The list of {@link AExperienceRole} used as a role configuration
     */
    CompletableFuture<RoleCalculationResult> updateUserRole(AUserExperience userExperience, List<AExperienceRole> roles, Integer currentLevel);


    /**
     * Synchronizes the state ({@link AExperienceRole}, {@link net.dv8tion.jda.api.entities.Role})
     * of all the users provided in the {@link AServer} object in the {@link AUserExperience}
     * and on the {@link net.dv8tion.jda.api.entities.Member} according
     * to how much experience the user has. Runs completely in the background.
     * @param server The {@link AServer} to update the users for
     */
    List<CompletableFuture<RoleCalculationResult>> syncUserRoles(AServer server);

    /**
     * Synchronizes the state ({@link AExperienceRole}, {@link net.dv8tion.jda.api.entities.Role})
     * of all the users provided in the {@link AServer} object in the {@link AUserExperience}
     * and on the {@link net.dv8tion.jda.api.entities.Member} according
     * to how much experience the user has. This method provides feedback back to the user in the provided {@link AChannel}
     * while the process is going own.
     * @param server The {@link AServer} to update users for
     * @param channel The {@link AChannel} in which the {@link dev.sheldan.abstracto.experience.models.templates.UserSyncStatusModel}
     *                should be posted to
     */
    CompletableFuture<Void> syncUserRolesWithFeedback(AServer server, AChannel channel);

    /**
     * Recalculates the role of a single user in a server and synchronize the {@link net.dv8tion.jda.api.entities.Role}
     * in the {@link net.dv8tion.jda.api.entities.Guild}
     * @param userExperience The {@link AUserExperience} to synchronize the role for
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
     * in the form of {@link dev.sheldan.abstracto.experience.models.templates.UserSyncStatusModel} to be rendered with a certain
     * template
     * @param experiences The list of {@link AUserExperience} to be working on
     * @param channel The {@link AChannel} used to provide feedback to the user
     * @param toExecute The {@link Consumer} which should be executed on each element of the passed list
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

    void syncRolesInStorage(List<RoleCalculationResult> results);
}

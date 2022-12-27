package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.model.LeaderBoard;
import dev.sheldan.abstracto.experience.model.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service providing the required mechanisms to provide experience tracking.
 * This includes manipulations on the {@link AUserExperience userExperience} table, container for the runtime experience, synchronizing the
 * user in the guild and retrieving {@link LeaderBoard leaderboard} data.
 */
public interface AUserExperienceService {
    String EXPERIENCE_GAIN_CHANNEL_GROUP_KEY = "experienceGain";
    void addExperience(Member member, Message message);

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

    CompletableFuture<Void> syncUserRolesWithFeedback(AServer server, MessageChannel messageChannel);

    CompletableFuture<Void> syncForSingleUser(AUserExperience userExperience, Member member, boolean changeRoles);

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
     * Disables the experience gain for a user directly. This sets the `experienceGainDisabled` on the respective {@link AUserExperience} object to true
     * @param userInAServer The {@link AUserInAServer} to disable experience gain for
     */
    void disableExperienceForUser(AUserInAServer userInAServer);

    /**
     * Enables the experience gain for a user directly. This sets the `experienceGainDisabled` on the respective {@link AUserExperience} object to false
     * @param userInAServer The {@link AUserInAServer} to enable experience for
     */
    void enableExperienceForUser(AUserInAServer userInAServer);

    boolean experienceGainEnabledInChannel(MessageChannel messageChannel);

    AUserExperience createUserExperienceForUser(AUserInAServer aUserInAServer, Long experience, Long messageCount);
    AUserExperience createUserExperienceForUser(AUserInAServer aUserInAServer, Long experience, Long messageCount, List<AExperienceLevel> levels);
}

package dev.sheldan.abstracto.experience.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LeaderBoardEntryResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository to manage the access to the table managed by {@link AUserExperience userExperience}
 */
@Repository
public interface UserExperienceRepository  extends JpaRepository<AUserExperience, Long> {

    /**
     * Finds all {@link AUserExperience userExperience} of the given {@link AServer server}
     * @param server The {@link AServer server} to retrieve the {@link AUserExperience userExperience} for
     * @return A complete list of {@link AUserExperience} of the given {@link AServer server}
     */
    List<AUserExperience> findByUser_ServerReference(AServer server);

    /**
     * Retrieves the {@link AUserExperience userExperience} ordered by experience, and applies the {@link Pageable pageable} to only filter out certain pages.
     * @param server The {@link AServer server} to retrieve the {@link AUserExperience userExperience} information for
     * @param pageable A {@link Pageable pageable} object to indicate the pages which should be retrieved, page size is 10
     * @return A list of {@link AUserExperience userExperience} of the given {@link AServer server} ordered by the experience of the users, paginated by the given
     * configuration
     */
    List<AUserExperience> findTop10ByUser_ServerReferenceOrderByExperienceDesc(AServer server, Pageable pageable);

    /**
     * This returns the {@link LeaderBoardEntryResult entryResult} object containing the information about the rank of a user in a server.
     * This query selects all the experience entries and returns the one associated with the provided user.
     * We need to select all of them, in order to find the rank of the member in the server
     * @param id The ID of an {@link dev.sheldan.abstracto.core.models.database.AUserInAServer userInAServer} search for
     * @param serverId The ID of the {@link AServer server} for which we are retrieving the experience
     * @return The {@link LeaderBoardEntryResult result} of this {@link dev.sheldan.abstracto.core.models.database.AUserInAServer userInAServer}
     * containing rank and experience information
     */
    @Query(value = "WITH user_experience_ranked AS" +
            "( " +
            "    SELECT us.id, us.experience, us.role_id, us.level_id, us.message_count, ROW_NUMBER() OVER ( ORDER BY experience DESC ) " +
            "    FROM user_experience us INNER JOIN user_in_server uis ON us.id = uis.user_in_server_id INNER JOIN server s ON s.id = uis.server_id WHERE s.id = :serverId" +
            ") " +
            "SELECT rank.id as \"id\", rank.experience as \"experience\", rank.message_count as \"messageCount\", rank.level_id as \"level\", rank.row_number as \"rank\"    " +
            "FROM user_experience_ranked rank " +
            "WHERE rank.id = :userInServerId", nativeQuery = true)
    LeaderBoardEntryResult getRankOfUserInServer(@Param("userInServerId") Long id, @Param("serverId") Long serverId);

    @Modifying(clearAutomatically = true)
    @Query("update AUserExperience u set u.currentExperienceRole = null where u.currentExperienceRole.id = :roleId")
    void removeExperienceRoleFromUsers(@Param("roleId") Long experienceRoleId);

}

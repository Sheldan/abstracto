package dev.sheldan.abstracto.experience.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.LeaderBoardEntryResult;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * Repository to manage the access to the table managed by {@link AUserExperience}
 */
@Repository
public interface UserExperienceRepository  extends JpaRepository<AUserExperience, Long> {

    /**
     * Finds all {@link AUserExperience} of the given {@link AServer}
     * @param server The {@link AServer} to retriev ethe {@link AUserExperience} for
     * @return A complete list of {@link AUserExperience} of the given {@link AServer}
     */
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<AUserExperience> findByUser_ServerReference(AServer server);

    /**
     * Retrieves the {@link AUserExperience} ordered by experience, and applies the {@link Pageable} to only filter out certain pages.
     * @param server The {@link AServer} to retrieve the {@link AUserExperience} information for
     * @param pageable A {@link Pageable} object to indicate the pages which should be retrieved, pagesize is 10
     * @return A list of {@link AUserExperience} of the given {@link AServer} ordered by the experience of the users, paginated by the given
     * configuration
     */
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<AUserExperience> findTop10ByUser_ServerReferenceOrderByExperienceDesc(AServer server, Pageable pageable);

    /**
     * This returns the {@link LeaderBoardEntryResult} object containing the information about the rank of a user in a server.
     * @param id The {@link dev.sheldan.abstracto.core.models.database.AUserInAServer} id to search for
     * @return the {@link LeaderBoardEntryResult} of this {@link dev.sheldan.abstracto.core.models.database.AUserInAServer}
     * containing rank and experience information
     */
    @Query(value = "WITH user_experience_ranked AS" +
            "( " +
            "    SELECT id, experience, experience_role_id, level_id, message_count, ROW_NUMBER() OVER ( ORDER BY experience DESC ) " +
            "    FROM user_experience" +
            ") " +
            "SELECT rank.id as \"id\", rank.experience as \"experience\", rank.message_count as \"messageCount\", rank.level_id as \"level\", rank.row_number as \"rank\"    " +
            "FROM user_experience_ranked rank " +
            "where rank.id = :userInServerId", nativeQuery = true)
    LeaderBoardEntryResult getRankOfUserInServer(@Param("userInServerId") Long id);
}

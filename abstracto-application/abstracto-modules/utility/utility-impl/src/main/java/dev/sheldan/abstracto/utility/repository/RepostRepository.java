package dev.sheldan.abstracto.utility.repository;

import dev.sheldan.abstracto.utility.models.database.Repost;
import dev.sheldan.abstracto.utility.models.database.embed.RepostIdentifier;
import dev.sheldan.abstracto.utility.models.database.result.RepostLeaderboardResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepostRepository extends JpaRepository<Repost, RepostIdentifier> {

    @Query(value = "WITH repost_rank AS " +
            "( " +
                "SELECT user_in_server_id as user_in_server_id, SUM(count) as repost_count, ROW_NUMBER() OVER ( ORDER BY SUM(count) DESC ) \n" +
                "FROM repost \n" +
                "WHERE server_id = :server_id " +
                "GROUP BY user_in_server_id \n" +
                "ORDER BY SUM(count) DESC \n" +
            " )" +
            "SELECT rank.user_in_server_id as userInServerId, rank.repost_count as repostCount, rank.row_number as rank " +
            "FROM repost_rank rank ",
            countQuery = "SELECT COUNT(1) FROM repost WHERE server_id = :server_id GROUP BY user_in_server_id",
            nativeQuery = true)
    List<RepostLeaderboardResult> findTopRepostingUsers(@Param("server_id") Long serverId, Pageable pageable);

    @Query(value = "WITH repost_rank AS " +
            "( " +
            "SELECT user_in_server_id as user_in_server_id, SUM(count) as repost_count, ROW_NUMBER() OVER ( ORDER BY SUM(count) DESC ) \n" +
            "FROM repost \n" +
            "WHERE server_id = :server_id " +
            "GROUP BY user_in_server_id \n" +
            "ORDER BY SUM(count) DESC \n" +
            " )" +
            "SELECT rank.user_in_server_id as userInServerId, rank.repost_count as repostCount, rank.row_number as rank    " +
            "FROM repost_rank rank " +
            "WHERE rank.user_in_server_id = :user_in_server_id " +
            "UNION ALL " +
            "SELECT :user_in_server_id as userInServerId, 0 as repostCount, 0 as rank " +
            "WHERE NOT EXISTS " +
            "(SELECT 1 " +
            "FROM repost_rank rank WHERE rank.user_in_server_id = :user_in_server_id" +
            ")",
            nativeQuery = true)
    RepostLeaderboardResult getRepostRankOfUserInServer(@Param("user_in_server_id") Long useInServerId, @Param("server_id") Long serverId);

    void deleteByServerId(Long serverId);

    void deleteByRepostId_UserInServerIdAndServerId(Long userInServerId, Long serverId);

}

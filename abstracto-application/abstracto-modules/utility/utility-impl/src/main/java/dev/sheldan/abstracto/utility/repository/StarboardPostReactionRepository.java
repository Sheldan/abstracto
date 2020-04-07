package dev.sheldan.abstracto.utility.repository;

import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.database.StarboardPostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StarboardPostReactionRepository extends JpaRepository<StarboardPostReaction, Long> {
    void deleteByReactorAndStarboardPost(AUser user, StarboardPost post);
    void deleteByStarboardPost(StarboardPost post);

    @Query(value = "SELECT r.reactor_id as userId, COUNT(*) AS starCount \n" +
            "FROM starboard_post_reaction r \n" +
            "INNER JOIN starboard_post p ON p.id = r.post_id\n" +
            "INNER JOIN channel c ON c.id = p.channel_id\n" +
            "WHERE c.server_id = :serverId\n" +
            "GROUP BY r.reactor_id \n" +
            "ORDER BY starCount DESC \n" +
            "LIMIT :count", nativeQuery = true)
    List<StarStatsUserResult> findTopStarGiverInServer(Long serverId, Integer count);

    @Query(value = "SELECT COUNT(*) \n" +
            "FROM starboard_post_reaction r \n" +
            "INNER JOIN starboard_post p ON p.id = r.post_id\n" +
            "INNER JOIN channel c ON c.id = p.channel_id\n" +
            "WHERE c.server_id = :serverId\n"
            , nativeQuery = true)
    Integer getReactionCountByServer(Long serverId);

    @Query(value = "SELECT p.poster as userId, COUNT(*) AS starCount \n" +
            "FROM starboard_post_reaction r \n" +
            "INNER JOIN starboard_post p ON p.id = r.post_id\n" +
            "INNER JOIN channel c ON c.id = p.channel_id\n" +
            "WHERE c.server_id = :serverId\n" +
            "GROUP BY p.poster \n" +
            "ORDER BY starCount DESC \n" +
            "LIMIT :count", nativeQuery = true)
    List<StarStatsUserResult> retrieveTopStarReceiverInServer(Long serverId, Integer count);
}

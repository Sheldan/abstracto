package dev.sheldan.abstracto.starboard.repository;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import dev.sheldan.abstracto.starboard.model.database.StarboardPostReaction;
import dev.sheldan.abstracto.starboard.repository.result.StarStatsGuildUserResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StarboardPostReactionRepository extends JpaRepository<StarboardPostReaction, Long> {

    void deleteByReactorAndStarboardPost(AUserInAServer user, StarboardPost post);

    void deleteByStarboardPost(StarboardPost post);

    long countByStarboardPost(StarboardPost post);

    @Query(value = "SELECT r.reactor_user_in_server_id as userId, COUNT(*) AS starCount \n" +
            "FROM starboard_post_reaction r \n" +
            "INNER JOIN starboard_post p ON p.id = r.post_id\n" +
            "WHERE p.server_id = :serverId\n" +
            " AND p.ignored = false\n" +
            "GROUP BY r.reactor_user_in_server_id \n" +
            "ORDER BY starCount DESC \n" +
            "LIMIT :count", nativeQuery = true)
    List<StarStatsGuildUserResult> findTopStarGiverInServer(Long serverId, Integer count);

    @Query(value = "SELECT COUNT(*) \n" +
            "FROM starboard_post_reaction r \n" +
            "INNER JOIN starboard_post p ON p.id = r.post_id\n" +
            "WHERE p.server_id = :serverId\n" +
            "AND p.ignored = false"
            , nativeQuery = true)
    Integer getReactionCountByServer(Long serverId);

    @Query(value = "SELECT p.author_user_in_server_id as userId, COUNT(*) AS starCount \n" +
            "FROM starboard_post_reaction r \n" +
            "INNER JOIN starboard_post p ON p.id = r.post_id\n" +
            "WHERE p.server_id = :serverId\n" +
            " AND p.ignored = false\n" +
            "GROUP BY p.author_user_in_server_id \n" +
            "ORDER BY starCount DESC \n" +
            "LIMIT :count", nativeQuery = true)
    List<StarStatsGuildUserResult> retrieveTopStarReceiverInServer(Long serverId, Integer count);
}

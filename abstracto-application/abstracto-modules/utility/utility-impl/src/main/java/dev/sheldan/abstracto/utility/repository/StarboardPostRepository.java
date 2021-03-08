package dev.sheldan.abstracto.utility.repository;

import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StarboardPostRepository extends JpaRepository<StarboardPost, Long> {

    StarboardPost findByPostMessageId(Long messageId);

    StarboardPost findByStarboardMessageId(Long messageId);

    boolean existsByStarboardMessageId(Long messageId);

    List<StarboardPost> findByServer_Id(Long serverId);

    @Query(value = "SELECT p.id, COUNT(*) AS starCount \n" +
            " FROM starboard_post p \n" +
            " INNER JOIN starboard_post_reaction r ON p.id = r.post_id\n" +
            " INNER JOIN user_in_server usi ON usi.user_in_server_id = p.author_user_in_server_id\n" +
            " WHERE p.server_id = :serverId\n" +
            " AND p.ignored = false\n" +
            " AND usi.user_id = :userId\n" +
            " GROUP BY p.id \n" +
            " ORDER BY starCount DESC \n" +
            " LIMIT :count", nativeQuery = true)
    List<Long> getTopStarboardPostsForUser(Long serverId, Long userId, Integer count);

    @Query(value = "SELECT COUNT(*) AS starCount\n" +
            "FROM starboard_post_reaction r \n" +
            " INNER JOIN starboard_post p ON p.id = r.post_id \n" +
            " INNER JOIN user_in_server usi ON usi.user_in_server_id = r.reactor_user_in_server_id \n" +
            " WHERE usi.user_id = :userId \n" +
            " AND p.ignored = false\n" +
            " AND r.server_id = :serverId", nativeQuery = true)
    Long getGivenStarsOfUserInServer(Long serverId, Long userId);

    @Query(value = "SELECT COUNT(*) AS starCount\n" +
            " FROM starboard_post_reaction r \n" +
            " INNER JOIN starboard_post p ON p.id = r.post_id \n" +
            " INNER JOIN user_in_server usi ON usi.user_in_server_id = p.author_user_in_server_id \n" +
            " WHERE p.author_user_in_server_id = usi.user_in_server_id \n" +
            " AND usi.user_id = :userId \n" +
            " AND p.ignored = false\n" +
            " AND r.server_id = :serverId", nativeQuery = true)
    Long getReceivedStarsOfUserInServer(Long serverId, Long userId);

}

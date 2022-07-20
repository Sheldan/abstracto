package dev.sheldan.abstracto.entertainment.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.entertainment.model.database.EconomyLeaderboardResult;
import dev.sheldan.abstracto.entertainment.model.database.EconomyUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EconomyUserRepository extends JpaRepository<EconomyUser, Long> {
    Optional<EconomyUser> findByUser(AUserInAServer aUserInAServer);

    @Query(value = "WITH economy_user_ranked AS" +
            "( " +
            "    SELECT eu.id, eu.credits, uis.user_id, ROW_NUMBER() OVER ( ORDER BY credits DESC ) " +
            "    FROM economy_user eu INNER JOIN user_in_server uis ON eu.id = uis.user_in_server_id INNER JOIN server s ON s.id = uis.server_id WHERE s.id = :serverId" +
            ") " +
            "SELECT rank.id as \"id\", rank.user_id as \"userid\", rank.credits as \"credits\", rank.row_number as \"rank\"    " +
            "FROM economy_user_ranked rank " +
            "WHERE rank.id = :userInServerId", nativeQuery = true)
    EconomyLeaderboardResult getRankOfUserInServer(@Param("userInServerId") Long id, @Param("serverId") Long serverId);

    @Query(value = "WITH economy_user_ranked AS" +
            "( " +
            "    SELECT eu.id, eu.credits, uis.user_id, ROW_NUMBER() OVER ( ORDER BY credits DESC ) " +
            "    FROM economy_user eu INNER JOIN user_in_server uis ON eu.id = uis.user_in_server_id INNER JOIN server s ON s.id = uis.server_id WHERE s.id = :serverId" +
            ") " +
            "SELECT rank.id as \"id\", rank.user_id as \"userid\", rank.credits as \"credits\", rank.row_number as \"rank\"    " +
            "FROM economy_user_ranked rank ", nativeQuery = true)
    List<EconomyLeaderboardResult> getRanksInServer(@Param("serverId") Long serverId);
    List<EconomyUser> findTop10ByServerOrderByCreditsDesc(AServer server, Pageable pageable);

    List<EconomyUser> findByServerOrderByCredits(AServer server);
}

package dev.sheldan.abstracto.experience.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.LeaderBoardEntryResult;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserExperienceRepository  extends JpaRepository<AUserExperience, Long> {
    List<AUserExperience> findByUser_ServerReference(AServer server);
    List<AUserExperience> findTop10ByUser_ServerReferenceOrderByExperienceDesc(AServer server, Pageable pageable);

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

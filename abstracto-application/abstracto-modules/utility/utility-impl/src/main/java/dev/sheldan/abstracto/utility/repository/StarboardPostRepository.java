package dev.sheldan.abstracto.utility.repository;

import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StarboardPostRepository extends JpaRepository<StarboardPost, Long> {

    StarboardPost findByPostMessageId(Long messageId);

    StarboardPost findByStarboardMessageId(Long messageId);

    boolean existsByStarboardMessageId(Long messageId);

    List<StarboardPost> findByServer_Id(Long serverId);

}

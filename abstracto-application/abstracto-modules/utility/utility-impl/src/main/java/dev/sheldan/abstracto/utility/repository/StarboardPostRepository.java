package dev.sheldan.abstracto.utility.repository;

import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface StarboardPostRepository extends JpaRepository<StarboardPost, Long> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    StarboardPost findByPostMessageId(Long messageId);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    StarboardPost findByStarboardMessageId(Long messageId);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsByStarboardMessageId(Long messageId);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<StarboardPost> findByServer(Long serverId);

}

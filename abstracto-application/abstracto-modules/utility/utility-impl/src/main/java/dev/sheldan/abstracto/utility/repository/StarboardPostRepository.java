package dev.sheldan.abstracto.utility.repository;

import dev.sheldan.abstracto.utility.models.StarboardPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StarboardPostRepository extends JpaRepository<StarboardPost, Long> {
    StarboardPost findByPostMessageId(Long messageId);
    StarboardPost findByStarboardMessageId(Long messageId);
    List<StarboardPost> findByStarboardChannelServerId(Long serverId);

}

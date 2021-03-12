package dev.sheldan.abstracto.repostdetection.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.repostdetection.model.database.PostedImage;
import dev.sheldan.abstracto.repostdetection.model.database.embed.PostIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostedImageRepository extends JpaRepository<PostedImage, PostIdentifier> {
    boolean existsByImageHashAndServerId(String hash,  Long serverId);
    Optional<PostedImage> findByImageHashAndServerId(String hash, Long serverId);
    boolean existsByPostId_MessageId(Long messageId);
    boolean existsByPostId_MessageIdAndPostId_PositionGreaterThan(Long messageId, Integer position);
    List<PostedImage> findByPostId_MessageId(Long messageId);
    void deleteByServer(AServer server);
    void deleteByPoster(AUserInAServer aUserInAServer);
}

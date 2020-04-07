package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;

import java.util.List;
import java.util.Optional;

public interface StarboardPostManagementService {
    StarboardPost createStarboardPost(CachedMessage starredMessage, AUserInAServer starredUser, AUserInAServer starringUser, AServerAChannelMessage starboardPost);
    void setStarboardPostMessageId(StarboardPost post, Long messageId);
    List<StarboardPost> retrieveTopPosts(Long serverId, Integer count);
    List<StarboardPost> retrieveAllPosts(Long serverId);
    Integer getPostCount(Long serverId);
    Optional<StarboardPost> findByMessageId(Long messageId);
    Optional<StarboardPost> findByStarboardPostId(Long postId);
    void setStarboardPostIgnored(Long starboardPostId, Boolean newValue);
    boolean isStarboardPost(Long starboardPostId);
    void removePost(StarboardPost starboardPost);
}

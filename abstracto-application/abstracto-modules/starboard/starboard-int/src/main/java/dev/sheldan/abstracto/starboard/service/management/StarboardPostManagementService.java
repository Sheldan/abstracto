package dev.sheldan.abstracto.starboard.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;

import java.util.List;
import java.util.Optional;

public interface StarboardPostManagementService {
    StarboardPost createStarboardPost(CachedMessage starredMessage, AUserInAServer starredUser, AServerAChannelMessage starboardPost);
    StarboardPost createStarboardPost(StarboardPost post);
    void setStarboardPostMessageId(StarboardPost post, Long messageId);
    List<StarboardPost> retrieveTopPosts(Long serverId, Integer count);
    List<StarboardPost> retrieveTopPostsForUserInServer(Long serverId, Long userId, Integer count);
    Long retrieveGivenStarsOfUserInServer(Long serverId, Long userId);
    Long retrieveReceivedStarsOfUserInServer(Long serverId, Long userId);
    List<StarboardPost> retrieveAllPosts(Long serverId);
    Long getPostCount(Long serverId);
    Optional<StarboardPost> findByMessageId(Long messageId);
    Optional<StarboardPost> findByStarboardPostId(Long postId);
    Optional<StarboardPost> findByStarboardPostMessageId(Long postId);
    void setStarboardPostIgnored(Long starboardPostId, Boolean newValue);
    boolean isStarboardPost(Long starboardPostId);
    void removePost(StarboardPost starboardPost);
}

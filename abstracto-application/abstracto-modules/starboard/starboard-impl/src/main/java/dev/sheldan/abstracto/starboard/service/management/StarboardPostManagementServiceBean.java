package dev.sheldan.abstracto.starboard.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import dev.sheldan.abstracto.starboard.repository.StarboardPostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Slf4j
public class StarboardPostManagementServiceBean implements StarboardPostManagementService {

    @Autowired
    private StarboardPostRepository repository;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public StarboardPost createStarboardPost(CachedMessage starredMessage, AUserInAServer starredUser, AServerAChannelMessage starboardPost) {
        AChannel build = channelManagementService.loadChannel(starredMessage.getChannelId());
        StarboardPost post = StarboardPost
                .builder()
                .author(starredUser)
                .postMessageId(starredMessage.getMessageId())
                .sourceChannel(build)
                .ignored(false)
                .reactions(new ArrayList<>())
                .server(starboardPost.getServer())
                .starboardMessageId(starboardPost.getMessageId())
                .starboardChannel(starboardPost.getChannel())
                .starredDate(Instant.now())
                .build();
        log.info("Persisting starboard post for message {} in channel {} in server {} on starboard at message {} in channel {} and server {} of user {}.",
                starredMessage.getMessageId(), starredMessage.getChannelId(), starredMessage.getServerId(),
                starboardPost.getMessageId(), starboardPost.getChannel().getId(), starboardPost.getServer().getId(),
                starredUser.getUserReference().getId());
        return repository.save(post);
    }

    @Override
    public StarboardPost createStarboardPost(StarboardPost post) {
        return repository.save(post);
    }

    @Override
    public void setStarboardPostMessageId(StarboardPost post, Long messageId) {
        post.setStarboardMessageId(messageId);
    }

    @Override
    public List<StarboardPost> retrieveTopPosts(Long serverId, Integer count) {
        List<StarboardPost> posts = retrieveAllPosts(serverId);
        posts.sort(Comparator.comparingInt(o -> o.getReactions().size()));
        Collections.reverse(posts);
        return posts.subList(0, Math.min(count, posts.size()));
    }

    @Override
    public List<StarboardPost> retrieveTopPostsForUserInServer(Long serverId, Long userId, Integer count) {
        List<Long> topPostIds = repository.getTopStarboardPostsForUser(serverId, userId, count);
        return repository.findAllById(topPostIds);
    }

    @Override
    public Long retrieveGivenStarsOfUserInServer(Long serverId, Long userId) {
        return repository.getGivenStarsOfUserInServer(serverId, userId);
    }

    @Override
    public Long retrieveReceivedStarsOfUserInServer(Long serverId, Long userId) {
        return repository.getReceivedStarsOfUserInServer(serverId, userId);
    }

    @Override
    public List<StarboardPost> retrieveAllPosts(Long serverId) {
        return repository.findByServer_Id(serverId);
    }

    @Override
    public Integer getPostCount(Long serverId) {
        return retrieveAllPosts(serverId).size();
    }

    @Override
    public Optional<StarboardPost> findByMessageId(Long messageId) {
        return Optional.ofNullable(repository.findByPostMessageId(messageId));
    }

    @Override
    public Optional<StarboardPost> findByStarboardPostId(Long postId) {
        return Optional.ofNullable(repository.findByStarboardMessageId(postId));
    }

    @Override
    public void setStarboardPostIgnored(Long messageId, Boolean newValue) {
        StarboardPost post = repository.findByStarboardMessageId(messageId);
        post.setIgnored(newValue);
        repository.save(post);
    }

    @Override
    public boolean isStarboardPost(Long messageId) {
        return repository.existsByStarboardMessageId(messageId);
    }

    @Override
    public void removePost(StarboardPost starboardPost) {
        starboardPost.getReactions().clear();
        repository.delete(starboardPost);
    }

}

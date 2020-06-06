package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.repository.StarboardPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class StarboardPostManagementServiceBean implements StarboardPostManagementService {

    @Autowired
    private StarboardPostRepository repository;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public StarboardPost createStarboardPost(CachedMessage starredMessage, AUserInAServer starredUser, AServerAChannelMessage starboardPost) {
        AChannel build = channelManagementService.loadChannel(starredMessage.getChannelId()).orElseThrow(() -> new ChannelNotFoundException(starredMessage.getChannelId(), starredMessage.getServerId()));
        StarboardPost post = StarboardPost
                .builder()
                .author(starredUser)
                .postMessageId(starredMessage.getMessageId())
                .sourceChanel(build)
                .ignored(false)
                .starboardMessageId(starboardPost.getMessageId())
                .starboardChannel(starboardPost.getChannel())
                .starredDate(Instant.now())
                .build();
        repository.save(post);
        return post;
    }

    @Override
    public void setStarboardPostMessageId(StarboardPost post, Long messageId) {
        post.setStarboardMessageId(messageId);
        repository.save(post);
    }

    @Override
    public List<StarboardPost> retrieveTopPosts(Long serverId, Integer count) {
        List<StarboardPost> posts = retrieveAllPosts(serverId);
        posts.sort(Comparator.comparingInt(o -> o.getReactions().size()));
        Collections.reverse(posts);
        return posts.subList(0, Math.min(count, posts.size()));
    }

    @Override
    public List<StarboardPost> retrieveAllPosts(Long serverId) {
        return repository.findByStarboardChannelServerId(serverId);
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

package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.converter.ChannelConverter;
import dev.sheldan.abstracto.core.models.converter.UserInServerConverter;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.repository.StarboardPostRepository;
import dev.sheldan.abstracto.utility.repository.converter.StarStatsUserConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class StarboardPostManagementServiceBean {

    @Autowired
    private StarboardPostRepository repository;

    @Autowired
    private StarStatsUserConverter converter;

    @Autowired
    private UserInServerConverter userInServerConverter;

    @Autowired
    private ChannelConverter channelConverter;

    public StarboardPost createStarboardPost(CachedMessage starredMessage, UserInServerDto starredUser, UserInServerDto starringUser, AServerAChannelMessage starboardPost) {

        AUserInAServer author = userInServerConverter.fromDto(starredUser);
        StarboardPost post = StarboardPost
                .builder()
                .author(author.getUserReference())
                .postMessageId(starredMessage.getMessageId())
                .starboardMessageId(starboardPost.getMessageId())
                .starboardChannel(channelConverter.fromDto(starboardPost.getChannel()))
                .starredDate(Instant.now())
                .build();
        repository.save(post);
        return post;
    }

    public void setStarboardPostMessageId(StarboardPost post, Long messageId) {
        post.setStarboardMessageId(messageId);
        repository.save(post);
    }

    public List<StarboardPost> retrieveTopPosts(Long serverId, Integer count) {
        List<StarboardPost> posts = retrieveAllPosts(serverId);
        posts.sort(Comparator.comparingInt(o -> o.getReactions().size()));
        Collections.reverse(posts);
        return posts.subList(0, Math.min(count, posts.size()));
    }

    public List<StarboardPost> retrieveAllPosts(Long serverId) {
        return repository.findByStarboardChannelServerId(serverId);
    }

    public Integer getPostCount(Long serverId) {
        return retrieveAllPosts(serverId).size();
    }

    public Optional<StarboardPost> findByMessageId(Long messageId) {
        return Optional.ofNullable(repository.findByPostMessageId(messageId));
    }

    public Optional<StarboardPost> findByStarboardPostId(Long postId) {
        return Optional.ofNullable(repository.findByStarboardMessageId(postId));
    }

    public void setStarboardPostIgnored(Long messageId, Boolean newValue) {
        StarboardPost post = repository.findByStarboardMessageId(messageId);
        post.setIgnored(newValue);
        repository.save(post);
    }

    public boolean isStarboardPost(Long messageId) {
        return repository.findByStarboardMessageId(messageId) != null;
    }

    public void removePost(StarboardPost starboardPost) {
        repository.deleteById(starboardPost.getId());
    }


}

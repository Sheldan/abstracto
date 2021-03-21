package dev.sheldan.abstracto.starboard.service;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.starboard.listener.StarboardPostState;
import dev.sheldan.abstracto.starboard.listener.StarboardPostUpdatedListener;
import dev.sheldan.abstracto.starboard.model.StarboardPostUpdatedModel;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StarboardPostListenerManager {

    @Autowired(required = false)
    private List<StarboardPostUpdatedListener> listeners;

    @Autowired
    @Qualifier("starboardStatusListenerExecutor")
    private TaskExecutor starboardStatusExecutor;

    @Autowired
    private StarboardPostListenerManager self;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ListenerService listenerService;

    public void sendStarboardPostCreatedEvent(Long userReactingId, StarboardPost post) {
        if(listeners == null || listeners.isEmpty()) {
            return;
        }
        ServerUser userReactingServerUser = ServerUser.builder().serverId(post.getServer().getId()).userId(userReactingId).build();
        StarboardPostUpdatedModel model = createStarboardStatusModel(post, userReactingServerUser);
        model.setNewState(StarboardPostState.CREATED);
        listeners.forEach(listener -> listenerService.executeFeatureAwareListener(listener, model));
    }

    public void sendStarboardPostDeletedEvent(StarboardPost post, ServerUser userReacting) {
        if(listeners == null || listeners.isEmpty()) {
            return;
        }
        StarboardPostUpdatedModel model = createStarboardStatusModel(post, userReacting);
        model.setNewState(StarboardPostState.DELETED);
    }

    private StarboardPostUpdatedModel createStarboardStatusModel(StarboardPost post, ServerUser userReacting) {
        Long serverId = post.getServer().getId();
        ServerUser starredUser = ServerUser
                .builder()
                .serverId(serverId)
                .userId(post.getAuthor().getUserReference().getId())
                .build();
        ServerChannelMessage starboardMessagePayLoad = ServerChannelMessage
                .builder()
                .serverId(serverId)
                .channelId(post.getStarboardChannel().getId())
                .messageId(post.getStarboardMessageId())
                .build();

        ServerChannelMessage starredMessage = ServerChannelMessage
                .builder()
                .serverId(serverId)
                .channelId(post.getSourceChannel().getId())
                .messageId(post.getPostMessageId())
                .build();

        List<Long> starrerIds = post.
                getReactions()
                .stream()
                .map(starboardPostReaction -> starboardPostReaction.getReactor().getUserReference().getId())
                .collect(Collectors.toList());
        return StarboardPostUpdatedModel
                .builder()
                .lastStarrer(userReacting)
                .starredUser(starredUser)
                .starboardMessage(starboardMessagePayLoad)
                .starredMessage(starredMessage)
                .allStarrer(starrerIds)
                .build();
    }

}

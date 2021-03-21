package dev.sheldan.abstracto.starboard.service;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.starboard.listener.StarboardPostDeletedListener;
import dev.sheldan.abstracto.starboard.model.StarboardPostDeletedModel;
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
public class StarboardPostDeletedListenerManager {

    @Autowired(required = false)
    private List<StarboardPostDeletedListener> listeners;

    @Autowired
    @Qualifier("starboardDeletedListenerExecutor")
    private TaskExecutor starboardDeletedExecutor;

    @Autowired
    private StarboardPostDeletedListenerManager self;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ListenerService listenerService;

    public void sendStarboardPostDeletedEvent(StarboardPost post, ServerUser userReacting) {
        if(listeners == null || listeners.isEmpty()) {
            return;
        }
        StarboardPostDeletedModel model = createStarboardStatusModel(post, userReacting);
        listeners.forEach(listener -> listenerService.executeFeatureAwareListener(listener, model, starboardDeletedExecutor));
    }

    private StarboardPostDeletedModel createStarboardStatusModel(StarboardPost post, ServerUser userReacting) {
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
        return StarboardPostDeletedModel
                .builder()
                .lastStarrer(userReacting)
                .starboardPostId(post.getId())
                .starredUser(starredUser)
                .starboardMessage(starboardMessagePayLoad)
                .starredMessage(starredMessage)
                .allStarrer(starrerIds)
                .build();
    }

}

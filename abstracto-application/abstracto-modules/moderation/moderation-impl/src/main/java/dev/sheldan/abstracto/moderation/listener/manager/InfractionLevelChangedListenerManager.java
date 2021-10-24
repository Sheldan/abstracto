package dev.sheldan.abstracto.moderation.listener.manager;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.moderation.listener.InfractionLevelChangedListener;
import dev.sheldan.abstracto.moderation.model.listener.InfractionLevelChangedEventModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class InfractionLevelChangedListenerManager {

    @Autowired(required = false)
    private List<InfractionLevelChangedListener> listeners;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("infractionLevelChangedExecutor")
    private TaskExecutor infractionLevelChangedExecutor;

    public void sendInfractionLevelChangedEvent(Integer newLevel, Integer oldLevel, Long newPoints, Long oldPoints, ServerUser serverUser) {
        if(listeners == null || listeners.isEmpty()) {
            return;
        }
        InfractionLevelChangedEventModel model = createInfractionChangedModel(newLevel, oldLevel, oldPoints, newPoints, serverUser);
        listeners.forEach(listener -> listenerService.executeFeatureAwareListener(listener, model, infractionLevelChangedExecutor));
    }

    private InfractionLevelChangedEventModel createInfractionChangedModel(Integer newLevel, Integer oldLevel, Long oldPoints, Long newPoints, ServerUser serverUser) {
        return InfractionLevelChangedEventModel
                .builder()
                .newLevel(newLevel)
                .oldLevel(oldLevel)
                .oldPoints(oldPoints)
                .newPoints(newPoints)
                .userId(serverUser.getUserId())
                .serverId(serverUser.getServerId())
                .build();
    }
}

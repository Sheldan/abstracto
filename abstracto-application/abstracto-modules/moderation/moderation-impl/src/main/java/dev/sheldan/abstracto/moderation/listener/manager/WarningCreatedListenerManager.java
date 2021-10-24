package dev.sheldan.abstracto.moderation.listener.manager;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.moderation.listener.WarningCreatedListener;
import dev.sheldan.abstracto.moderation.model.listener.WarningCreatedEventModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class WarningCreatedListenerManager {

    @Autowired(required = false)
    private List<WarningCreatedListener> listeners;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("warningCreatedExecutor")
    private TaskExecutor warningCreatedExecutor;

    public void sendWarningCreatedEvent(ServerSpecificId warnId, ServerUser warnedUser, ServerUser warningUser,
                                        String reason, ServerChannelMessage warnCommand) {
        if(listeners == null || listeners.isEmpty()) {
            return;
        }
        WarningCreatedEventModel model = createWarningCreatedEventModel(warnId, warnedUser, warningUser, reason, warnCommand);
        listeners.forEach(listener -> listenerService.executeFeatureAwareListener(listener, model, warningCreatedExecutor));
    }

    private WarningCreatedEventModel createWarningCreatedEventModel(ServerSpecificId warnId, ServerUser warnedUser,
                                                                    ServerUser warningUser, String reason, ServerChannelMessage warnCommandMessage) {
        return WarningCreatedEventModel
                .builder()
                .warningId(warnId.getId())
                .serverId(warnId.getServerId())
                .warningUserId(warningUser.getUserId())
                .warnedUserId(warnedUser.getUserId())
                .reason(reason)
                .warningChannelId(warnCommandMessage.getChannelId())
                .warningMessageId(warnCommandMessage.getMessageId())
                .build();
    }
}

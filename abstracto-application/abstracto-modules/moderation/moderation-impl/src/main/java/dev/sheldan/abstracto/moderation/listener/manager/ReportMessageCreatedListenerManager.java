package dev.sheldan.abstracto.moderation.listener.manager;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.moderation.listener.ReportMessageCreatedListener;
import dev.sheldan.abstracto.moderation.model.listener.ReportMessageCreatedModel;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportMessageCreatedListenerManager {

    @Autowired(required = false)
    private List<ReportMessageCreatedListener> listeners;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("reportMessageCreatedExecutor")
    private TaskExecutor reportMessageCreatedExecutor;

    public void sendReportMessageCreatedEvent(CachedMessage cachedMessage, Message message, ServerUser reporter) {
        if(listeners == null || listeners.isEmpty()) {
            return;
        }
        ReportMessageCreatedModel model = createEventModel(cachedMessage, message, reporter);
        listeners.forEach(listener -> listenerService.executeFeatureAwareListener(listener, model, reportMessageCreatedExecutor));
    }

    private ReportMessageCreatedModel createEventModel(CachedMessage reportedMessage, Message message, ServerUser reporter) {
        return ReportMessageCreatedModel
                .builder()
                .reportedMessage(ServerChannelMessage.fromCachedMessage(reportedMessage))
                .reportMessage(ServerChannelMessage.fromMessage(message))
                .reporter(reporter)
                .build();
    }
}

package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.MessageCache;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Slf4j
public class AsyncMessageReceivedListenerBean extends ListenerAdapter {
    @Autowired
    private MessageCache messageCache;

    @Autowired(required = false)
    private List<AsyncMessageReceivedListener> listenerList;

    @Autowired
    @Qualifier("messageReceivedExecutor")
    private TaskExecutor messageReceivedExecutor;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private ObservationRegistry observationRegistry;

    @Override
    @Transactional
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if(listenerList == null) return;
        if(!event.isFromGuild()) return;
        Observation observation = Observation.createNotStarted("async-message-received", this.observationRegistry);
        observation.lowCardinalityKeyValue("some-tag", "some-value");
        observation.observe(() -> {
            messageCache.putMessageInCache(event.getMessage());
            MessageReceivedModel model = getModel(event);
            listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, messageReceivedExecutor));
        });
    }

    private MessageReceivedModel getModel(MessageReceivedEvent event) {
        return MessageReceivedModel
                .builder()
                .message(event.getMessage())
                .build();
    }
}

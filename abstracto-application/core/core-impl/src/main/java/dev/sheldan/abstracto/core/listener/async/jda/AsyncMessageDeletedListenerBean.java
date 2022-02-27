package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.MessageDeletedModel;
import dev.sheldan.abstracto.core.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

@Component
@Slf4j
public class AsyncMessageDeletedListenerBean extends ListenerAdapter {
    @Autowired(required = false)
    private List<AsyncMessageDeletedListener> listenerList;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    @Qualifier("messageDeletedExecutor")
    private TaskExecutor messageDeletedListenerExecutor;

    @Autowired
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {
        if(listenerList == null) return;
        Consumer<CachedMessage> cachedMessageConsumer = cachedMessage -> {
            MessageDeletedModel model = getModel(cachedMessage);
            listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, messageDeletedListenerExecutor));
        };
        messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong())
                .thenAccept(cachedMessageConsumer)
                .exceptionally(throwable -> {
                    log.error("Message retrieval {} from cache failed. ", event.getMessageIdLong(), throwable);
                    return null;
                });
    }

    private MessageDeletedModel getModel(CachedMessage cachedMessage) {
        return MessageDeletedModel.builder().cachedMessage(cachedMessage).build();
    }

}

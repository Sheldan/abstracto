package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.CacheEntityService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AsyncPrivateMessageReceivedListenerBean extends ListenerAdapter {

    @Autowired
    private BotService botService;

    @Autowired
    private ExceptionService exceptionService;

    @Autowired(required = false)
    private List<AsyncPrivateMessageReceivedListener> privateMessageReceivedListeners;

    @Autowired
    private AsyncPrivateMessageReceivedListenerBean self;

    @Autowired
    private CacheEntityService cacheEntityService;

    @Autowired
    @Qualifier("privateMessageReceivedExecutor")
    private TaskExecutor privateMessageReceivedExecutor;

    @Override
    @Transactional
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        if(privateMessageReceivedListeners == null) return;
        if(event.getAuthor().getId().equals(botService.getInstance().getSelfUser().getId())) {
            return;
        }
        cacheEntityService.buildCachedMessageFromMessage(event.getMessage()).thenAccept(cachedMessage ->
            privateMessageReceivedListeners.forEach(messageReceivedListener -> {
                try {
                    CompletableFuture.runAsync(() ->
                        self.executeIndividualPrivateMessageReceivedListener(cachedMessage, messageReceivedListener)
                        , privateMessageReceivedExecutor)
                        .exceptionally(throwable -> {
                            log.error("Async private message receiver listener {} failed.", messageReceivedListener, throwable);
                            return null;
                        });
                } catch (Exception e) {
                    log.error("Private message received {} had exception when executing.", messageReceivedListener, e);
                    exceptionService.reportExceptionToPrivateMessageReceivedContext(e, event);
                }
            })
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeIndividualPrivateMessageReceivedListener(CachedMessage cachedMessage, AsyncPrivateMessageReceivedListener messageReceivedListener) {
        log.debug("Executing private message listener {} for member {}.", messageReceivedListener.getClass().getName(), cachedMessage.getAuthor().getAuthorId());
        messageReceivedListener.execute(cachedMessage);
    }

}

package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.ReactionClearedModel;
import dev.sheldan.abstracto.core.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AsyncReactionClearedListenerBean extends ListenerAdapter {

    @Autowired
    private MessageCache messageCache;

    @Autowired(required = false)
    private List<AsyncReactionClearedListener> clearedListenerList;

    @Autowired
    @Qualifier("reactionClearedExecutor")
    private TaskExecutor reactionClearedExecutor;

    @Autowired
    private ListenerService listenerServiceBean;

    @Override
    @Transactional
    public void onMessageReactionRemoveAll(@Nonnull MessageReactionRemoveAllEvent event) {
        CompletableFuture<CachedMessage> asyncMessageFromCache = messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
        asyncMessageFromCache.thenAccept(cachedMessage -> {
            cachedMessage.getReactions().clear();
            messageCache.putMessageInCache(cachedMessage);
            ReactionClearedModel model = getModel(event, cachedMessage);
            if(clearedListenerList == null) {
                return;
            }
            clearedListenerList.forEach(asyncReactionClearedListener -> listenerServiceBean.executeFeatureAwareListener(asyncReactionClearedListener, model, reactionClearedExecutor));
        }).exceptionally(throwable -> {
            log.error("Message retrieval from cache failed for message {}", event.getMessageIdLong(), throwable);
            return null;
        });
    }

    private ReactionClearedModel getModel(MessageReactionRemoveAllEvent event, CachedMessage message) {
        return ReactionClearedModel
                .builder()
                .message(message)
                .channel(event.getChannel())
                .build();
    }

}

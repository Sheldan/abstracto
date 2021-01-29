package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Component
@Slf4j
public class AsyncMessageUpdatedListenerBean extends ListenerAdapter {
    @Autowired(required = false)
    private List<AsyncMessageTextUpdatedListener> listener;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private AsyncMessageUpdatedListenerBean self;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    @Qualifier("messageUpdatedExecutor")
    private TaskExecutor messageUpdatedExecutor;

    @Override
    @Transactional
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if(listener == null) return;
        Message message = event.getMessage();
        messageCache.getMessageFromCache(message.getGuild().getIdLong(), message.getTextChannel().getIdLong(), event.getMessageIdLong())
                .thenAcceptBoth(messageCache.putMessageInCache(message), (oldCache, newCache) ->  self.executeListeners(newCache, oldCache))
        .exceptionally(throwable -> {
            log.error("Message retrieval {} from cache failed. ", event.getMessage().getId(), throwable);
            return null;
        });
    }

    @Transactional
    public void executeListeners(CachedMessage updatedMessage, CachedMessage oldMessage) {
        listener.forEach(messageTextUpdatedListener ->
            CompletableFuture
                .runAsync(() ->  self.executeIndividualMessageUpdatedListener(updatedMessage, oldMessage, messageTextUpdatedListener), messageUpdatedExecutor)
                .exceptionally(throwable -> {
                    log.error("Async message receiver listener {} failed.", messageTextUpdatedListener, throwable);
                    return null;
                })
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeIndividualMessageUpdatedListener(CachedMessage updatedMessage, CachedMessage cachedMessage, AsyncMessageTextUpdatedListener messageTextUpdatedListener) {
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(messageTextUpdatedListener.getFeature());
        if(!featureFlagService.isFeatureEnabled(feature, cachedMessage.getServerId())) {
            return;
        }
        try {
            messageTextUpdatedListener.execute(cachedMessage, updatedMessage);
        } catch (AbstractoRunTimeException e) {
            log.error(String.format("Failed to execute listener. %s", messageTextUpdatedListener.getClass().getName()), e);
        }
    }

}

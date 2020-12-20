package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AsyncReactionClearedListenerBean extends ListenerAdapter {

    @Autowired
    private CacheEntityService cacheEntityService;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired(required = false)
    private List<AsyncReactionClearedListener> clearedListenerList;

    @Autowired
    private AsyncReactionClearedListenerBean self;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private BotService botService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    @Qualifier("reactionClearedExecutor")
    private TaskExecutor reactionClearedExecutor;

    @Transactional
    public void callClearListeners(CachedMessage cachedMessage) {
        if(clearedListenerList == null) return;
        clearedListenerList.forEach(reactionRemovedListener ->
            CompletableFuture.runAsync(() ->
                self.callConcreteListener(cachedMessage, reactionRemovedListener)
            , reactionClearedExecutor)
            .exceptionally(throwable -> {
                log.error("Async reaction cleared listener {} failed with exception.", reactionRemovedListener, throwable);
                return null;
            })
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void callConcreteListener(CachedMessage cachedMessage, AsyncReactionClearedListener reactionRemovedListener) {
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(reactionRemovedListener.getFeature());
        if(!featureFlagService.isFeatureEnabled(feature, cachedMessage.getServerId())) {
            return;
        }
        try {
            reactionRemovedListener.executeReactionCleared(cachedMessage);
        } catch (AbstractoRunTimeException e) {
            log.warn(String.format("Failed to execute reaction clear listener %s.", reactionRemovedListener.getClass().getName()), e);
        }
    }

    @Override
    @Transactional
    public void onGuildMessageReactionRemoveAll(@Nonnull GuildMessageReactionRemoveAllEvent event) {
        CompletableFuture<CachedMessage> asyncMessageFromCache = messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
        asyncMessageFromCache.thenAccept(cachedMessage -> {
            cachedMessage.getReactions().clear();
            messageCache.putMessageInCache(cachedMessage);
            self.callClearListeners(cachedMessage);
        }) .exceptionally(throwable -> {
            log.error("Message retrieval from cache failed for message {}", event.getMessageIdLong(), throwable);
            return null;
        });
    }

}

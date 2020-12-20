package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
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
import java.util.function.Consumer;

@Component
@Slf4j
public class AsyncMessageDeletedListenerBean extends ListenerAdapter {
    @Autowired(required = false)
    private List<AsyncMessageDeletedListener> listener;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private AsyncMessageDeletedListenerBean self;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private BotService botService;

    @Autowired
    @Qualifier("messageDeletedExecutor")
    private TaskExecutor messageDeletedListenerExecutor;

    @Override
    @Transactional
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        if(listener == null) return;
        Consumer<CachedMessage> cachedMessageConsumer = cachedMessage -> self.executeListener(cachedMessage);
        messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong())
                .thenAccept(cachedMessageConsumer)
                .exceptionally(throwable -> {
                    log.error("Message retrieval {} from cache failed. ", event.getMessageIdLong(), throwable);
                    return null;
                });
    }

    @Transactional
    public void executeListener(CachedMessage cachedMessage) {
        listener.forEach(messageDeletedListener ->
            CompletableFuture.runAsync(() ->
                self.executeIndividualMessageDeletedListener(cachedMessage, messageDeletedListener)
            , messageDeletedListenerExecutor).exceptionally(throwable -> {
                log.error("Async message deleted {} failed with exception.", messageDeletedListener, throwable);
                return null;
            })
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeIndividualMessageDeletedListener(CachedMessage cachedMessage, AsyncMessageDeletedListener messageDeletedListener) {
        log.trace("Executing message deleted listener {} for message {} in guild {}.", messageDeletedListener.getClass().getName(), cachedMessage.getMessageId(), cachedMessage.getMessageId());
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(messageDeletedListener.getFeature());
        if(!featureFlagService.isFeatureEnabled(feature, cachedMessage.getServerId())) {
            return;
        }
        try {
            messageDeletedListener.execute(cachedMessage);
        } catch (AbstractoRunTimeException e) {
            log.error("Listener {} failed with exception:", messageDeletedListener.getClass().getName(), e);
        }
    }

}

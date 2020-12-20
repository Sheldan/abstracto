package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
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
public class AsyncMessageReceivedListenerBean extends ListenerAdapter {
    @Autowired
    private MessageCache messageCache;

    @Autowired(required = false)
    private List<AsyncMessageReceivedListener> listenerList;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private BotService botService;

    @Autowired
    private ExceptionService exceptionService;

    @Autowired
    @Qualifier("messageReceivedExecutor")
    private TaskExecutor messageReceivedExecutor;

    @Autowired
    private AsyncMessageReceivedListenerBean self;

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if(listenerList == null) return;
        messageCache.putMessageInCache(event.getMessage()).thenAccept(message -> {
            for (AsyncMessageReceivedListener messageReceivedListener : listenerList) {
                CompletableFuture.runAsync(() -> self.executeIndividualGuildMessageReceivedListener(message, messageReceivedListener), messageReceivedExecutor)
                    .exceptionally(throwable -> {
                        log.error("Async message received listener {} failed.", messageReceivedListener, throwable);
                        return null;
                    });
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeIndividualGuildMessageReceivedListener(CachedMessage cachedMessage, AsyncMessageReceivedListener messageReceivedListener) {
        try {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(messageReceivedListener.getFeature());
            if (!featureFlagService.isFeatureEnabled(feature, cachedMessage.getServerId())) {
                return;
            }
            messageReceivedListener.execute(cachedMessage);
        } catch (Exception e) {
            log.error("Async listener {} had exception when executing.", messageReceivedListener, e);
        }
    }
}

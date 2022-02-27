package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.ReactionClearedModel;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ReactionClearedListenerBean extends ListenerAdapter {

    @Autowired
    private CacheEntityService cacheEntityService;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired(required = false)
    private List<ReactionClearedListener> clearedListenerList;

    @Autowired
    private ReactionClearedListenerBean self;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private BotService botService;

    @Autowired
    private ListenerService listenerService;

    public void callClearListeners(@Nonnull MessageReactionRemoveAllEvent event, CachedMessage cachedMessage) {
        if(clearedListenerList == null) return;
        ReactionClearedModel model = getModel(event, cachedMessage);
        clearedListenerList.forEach(reactionRemovedListener ->
            listenerService.executeFeatureAwareListener(reactionRemovedListener, model)
        );
    }

    private ReactionClearedModel getModel(MessageReactionRemoveAllEvent event, CachedMessage message) {
        return ReactionClearedModel
                .builder()
                .message(message)
                .channel(event.getChannel())
                .build();
    }

    @Override
    @Transactional
    public void onMessageReactionRemoveAll(@Nonnull MessageReactionRemoveAllEvent event) {
        CompletableFuture<CachedMessage> asyncMessageFromCache = messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
        asyncMessageFromCache.thenAccept(cachedMessage -> {
            cachedMessage.getReactions().clear();
            messageCache.putMessageInCache(cachedMessage);
            self.callClearListeners(event, cachedMessage);
        }) .exceptionally(throwable -> {
            log.error("Message retrieval from cache failed for message {}", event.getMessageIdLong(), throwable);
            return null;
        });
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(clearedListenerList);
    }

}

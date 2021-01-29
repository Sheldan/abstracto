package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
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
    private EmoteService emoteService;

    public void callClearListeners(@Nonnull GuildMessageReactionRemoveAllEvent event, CachedMessage cachedMessage) {
        if(clearedListenerList == null) return;
        clearedListenerList.forEach(reactionRemovedListener ->
            self.executeIndividualListener(event, cachedMessage, reactionRemovedListener)
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeIndividualListener(@NotNull GuildMessageReactionRemoveAllEvent event, CachedMessage cachedMessage, ReactionClearedListener reactionRemovedListener) {
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(reactionRemovedListener.getFeature());
        if(!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
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

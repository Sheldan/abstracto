package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.service.CacheEntityService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AsyncEmoteDeletedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncEmoteDeletedListener> deletedListeners;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private AsyncEmoteDeletedListenerBean self;

    @Autowired
    @Qualifier("emoteDeletedExecutor")
    private TaskExecutor emoteCreatedExecutor;

    @Autowired
    private CacheEntityService cacheEntityService;

    @Override
    @Transactional
    public void onEmoteRemoved(@NotNull EmoteRemovedEvent event) {
        if(deletedListeners == null) return;
        CachedEmote cachedEmote = cacheEntityService.getCachedEmoteFromEmote(event.getEmote(), event.getGuild());

        deletedListeners.forEach(emoteUpdatedListener ->
            CompletableFuture.runAsync(() ->
                self.executeDeletedListener(emoteUpdatedListener, cachedEmote, event.getGuild().getIdLong())
            , emoteCreatedExecutor)
            .exceptionally(throwable -> {
                log.error("Async join listener {} failed with exception.", emoteUpdatedListener, throwable);
                return null;
            })
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeDeletedListener(AsyncEmoteDeletedListener listener, CachedEmote deletedEmote, Long serverId) {
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(listener.getFeature());
        if (!featureFlagService.isFeatureEnabled(feature, serverId)) {
            return;
        }
        if(!featureModeService.necessaryFeatureModesMet(listener, serverId)) {
            return;
        }
        listener.emoteDeleted(deletedEmote);
    }
}

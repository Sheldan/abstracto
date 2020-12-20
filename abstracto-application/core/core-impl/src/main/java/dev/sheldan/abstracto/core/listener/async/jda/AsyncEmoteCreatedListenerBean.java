package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.service.CacheEntityService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AsyncEmoteCreatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncEmoteCreatedListener> createdListeners;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private AsyncEmoteCreatedListenerBean self;

    @Autowired
    @Qualifier("emoteCreatedExecutor")
    private TaskExecutor emoteCreatedExecutor;

    @Autowired
    private CacheEntityService cacheEntityService;

    @Override
    @Transactional
    public void onEmoteAdded(@NotNull EmoteAddedEvent event) {
        if(createdListeners == null) return;
        CachedEmote cachedEmote = cacheEntityService.getCachedEmoteFromEmote(event.getEmote(), event.getGuild());
        createdListeners.forEach(emoteUpdatedListener ->
            CompletableFuture.runAsync(() ->
                self.executeCreatedListener(emoteUpdatedListener, cachedEmote, event.getGuild().getIdLong())
            , emoteCreatedExecutor)
            .exceptionally(throwable -> {
                log.error("Async join listener {} failed with exception.", emoteUpdatedListener, throwable);
                return null;
            })
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeCreatedListener(AsyncEmoteCreatedListener listener, CachedEmote createDdEmote, Long serverId) {
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(listener.getFeature());
        if (!featureFlagService.isFeatureEnabled(feature, serverId)) {
            return;
        }
        if(!featureModeService.necessaryFeatureModesMet(listener, serverId)) {
            return;
        }
        listener.emoteCreated(createDdEmote);
    }
}

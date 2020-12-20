package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.service.CacheEntityService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateNameEvent;
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
public class AsyncEmoteUpdatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncEmoteUpdatedListener> updatedListeners;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private AsyncEmoteUpdatedListenerBean self;

    @Autowired
    @Qualifier("emoteUpdatedExecutor")
    private TaskExecutor emoteUpdatedExecutor;

    @Autowired
    private CacheEntityService cacheEntityService;

    @Override
    public void onEmoteUpdateName(@NotNull EmoteUpdateNameEvent event) {
        if(updatedListeners == null) return;
        CachedEmote cachedEmote = cacheEntityService.getCachedEmoteFromEmote(event.getEmote(), event.getGuild());
        updatedListeners.forEach(emoteUpdatedListener ->
            CompletableFuture.runAsync(() ->
                self.executeUpdatedListener(emoteUpdatedListener, cachedEmote, event.getOldName(), event.getNewName(), event.getGuild().getIdLong())
            , emoteUpdatedExecutor)
            .exceptionally(throwable -> {
                log.error("Async join listener {} failed with exception.", emoteUpdatedListener, throwable);
                return null;
            })
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeUpdatedListener(AsyncEmoteUpdatedListener listener, CachedEmote updatedEmote, String oldName, String newName, Long serverId) {
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(listener.getFeature());
        if (!featureFlagService.isFeatureEnabled(feature, serverId)) {
            return;
        }
        if(!featureModeService.necessaryFeatureModesMet(listener, serverId)) {
            return;
        }
        listener.emoteUpdated(updatedEmote, oldName, newName);
    }

}

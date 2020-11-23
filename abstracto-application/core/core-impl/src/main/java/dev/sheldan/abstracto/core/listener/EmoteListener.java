package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.api.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class EmoteListener extends ListenerAdapter {

    @Autowired
    private List<EmoteCreatedListener> createdListeners;

    @Autowired
    private List<EmoteDeletedListener> deletedListeners;

    @Autowired
    private List<EmoteUpdatedListener> updatedListeners;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    @Lazy
    private EmoteListener self;

    @Override
    @Transactional
    public void onEmoteAdded(@NotNull EmoteAddedEvent event) {
        createdListeners.forEach(listener -> {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(listener.getFeature());
            if (!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                return;
            }
            if(!featureModeService.necessaryFeatureModesMet(listener, event.getGuild().getIdLong())) {
                return;
            }
            self.executeCreatedListener(listener, event.getEmote());
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeCreatedListener(EmoteCreatedListener listener, Emote createDdEmote) {
        listener.emoteCreated(createDdEmote);
    }

    @Override
    public void onEmoteRemoved(@NotNull EmoteRemovedEvent event) {
        deletedListeners.forEach(listener -> {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(listener.getFeature());
            if (!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                return;
            }
            if(!featureModeService.necessaryFeatureModesMet(listener, event.getGuild().getIdLong())) {
                return;
            }
            self.executeDeletedListener(listener, event.getEmote());
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeDeletedListener(EmoteDeletedListener listener, Emote createDdEmote) {
        listener.emoteDeleted(createDdEmote);
    }

    @Override
    public void onEmoteUpdateName(@NotNull EmoteUpdateNameEvent event) {
        updatedListeners.forEach(emoteUpdatedListener -> {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(emoteUpdatedListener.getFeature());
            if (!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                return;
            }
            if(!featureModeService.necessaryFeatureModesMet(emoteUpdatedListener, event.getGuild().getIdLong())) {
                return;
            }
            self.executeUpdatedListener(emoteUpdatedListener, event.getEmote(), event.getOldName(), event.getNewName());
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeUpdatedListener(EmoteUpdatedListener listener, Emote updatedEmote, String oldName, String newName) {
        listener.emoteUpdated(updatedEmote, oldName, newName);
    }

    @PostConstruct
    public void postConstruct() {
        createdListeners.sort(Comparator.comparing(Prioritized::getPriority).reversed());
        deletedListeners.sort(Comparator.comparing(Prioritized::getPriority).reversed());
        updatedListeners.sort(Comparator.comparing(Prioritized::getPriority).reversed());
    }
}

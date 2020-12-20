package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class EmoteUpdatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<EmoteUpdatedListener> updatedListeners;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    @Lazy
    private EmoteUpdatedListenerBean self;

    @Override
    public void onEmoteUpdateName(@NotNull EmoteUpdateNameEvent event) {
        if(updatedListeners == null) return;
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
        BeanUtils.sortPrioritizedListeners(updatedListeners);
    }
}

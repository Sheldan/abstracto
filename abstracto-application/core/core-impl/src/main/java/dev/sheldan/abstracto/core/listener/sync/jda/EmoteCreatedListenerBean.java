package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class EmoteCreatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<EmoteCreatedListener> createdListeners;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    @Lazy
    private EmoteCreatedListenerBean self;

    @Override
    @Transactional
    public void onEmoteAdded(@NotNull EmoteAddedEvent event) {
        if(createdListeners == null) return;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeCreatedListener(EmoteCreatedListener listener, Emote createDdEmote) {
        listener.emoteCreated(createDdEmote);
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(createdListeners);
    }
}

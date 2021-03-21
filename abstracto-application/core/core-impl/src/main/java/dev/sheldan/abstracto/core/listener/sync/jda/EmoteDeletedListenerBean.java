package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.EmoteDeletedModel;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class EmoteDeletedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<EmoteDeletedListener> deletedListeners;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    @Lazy
    private ListenerService listenerService;

    @Override
    public void onEmoteRemoved(@NotNull EmoteRemovedEvent event) {
        if(deletedListeners == null) return;
        EmoteDeletedModel model = getModel(event);
        deletedListeners.forEach(listener -> listenerService.executeFeatureAwareListener(listener, model));
    }

    private EmoteDeletedModel getModel(EmoteRemovedEvent event) {
        return EmoteDeletedModel.builder().emote(event.getEmote()).build();
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(deletedListeners);
    }
}

package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.EmoteCreatedModel;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class EmoteCreatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<EmoteCreatedListener> createdListeners;

    @Autowired
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onEmoteAdded(@NotNull EmoteAddedEvent event) {
        if(createdListeners == null) return;
        EmoteCreatedModel model = getModel(event);
        createdListeners.forEach(listener -> listenerService.executeFeatureAwareListener(listener, model));
    }

    private EmoteCreatedModel getModel(EmoteAddedEvent event) {
        return EmoteCreatedModel.builder().emote(event.getEmote()).build();
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(createdListeners);
    }
}

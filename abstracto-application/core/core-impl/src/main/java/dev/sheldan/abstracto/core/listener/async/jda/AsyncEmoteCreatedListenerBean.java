package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.EmoteCreatedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AsyncEmoteCreatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncEmoteCreatedListener> listenerList;

    @Autowired
    @Qualifier("emoteCreatedExecutor")
    private TaskExecutor emoteCreatedListenerExecutor;

    @Autowired
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onEmoteAdded(@NotNull EmoteAddedEvent event) {
        if(listenerList == null) return;
        EmoteCreatedModel model = getModel(event);
        listenerList.forEach(joinListener -> listenerService.executeFeatureAwareListener(joinListener, model, emoteCreatedListenerExecutor));
    }

    private EmoteCreatedModel getModel(EmoteAddedEvent event) {
        return EmoteCreatedModel.builder().emote(event.getEmote()).build();
    }
}

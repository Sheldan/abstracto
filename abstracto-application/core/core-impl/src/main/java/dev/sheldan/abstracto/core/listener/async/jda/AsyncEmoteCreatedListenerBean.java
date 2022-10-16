package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.EmoteCreatedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
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
    public void onEmojiAdded(@Nonnull EmojiAddedEvent event) {
        if(listenerList == null) return;
        EmoteCreatedModel model = getModel(event);
        listenerList.forEach(joinListener -> listenerService.executeFeatureAwareListener(joinListener, model, emoteCreatedListenerExecutor));
    }

    private EmoteCreatedModel getModel(EmojiAddedEvent event) {
        return EmoteCreatedModel.builder().emote(event.getEmoji()).build();
    }
}

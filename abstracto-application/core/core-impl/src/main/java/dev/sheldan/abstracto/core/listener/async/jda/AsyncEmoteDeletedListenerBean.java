package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.EmoteDeletedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
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
public class AsyncEmoteDeletedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncEmoteDeletedListener> listenerList;

    @Autowired
    @Qualifier("emoteDeletedExecutor")
    private TaskExecutor emoteDeletedListenerExecutor;

    @Autowired
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onEmojiRemoved(@NotNull EmojiRemovedEvent event) {
        if(listenerList == null) return;
        EmoteDeletedModel model = getModel(event);
        listenerList.forEach(deletedListener -> listenerService.executeFeatureAwareListener(deletedListener, model, emoteDeletedListenerExecutor));
    }

    private EmoteDeletedModel getModel(EmojiRemovedEvent event) {
        return EmoteDeletedModel.builder().emote(event.getEmoji()).build();
    }
}

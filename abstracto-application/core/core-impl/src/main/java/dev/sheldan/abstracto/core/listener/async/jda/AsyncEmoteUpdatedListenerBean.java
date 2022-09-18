package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.EmoteNameUpdatedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateNameEvent;
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
public class AsyncEmoteUpdatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncEmoteNameUpdatedListener> listenerList;

    @Autowired
    @Qualifier("emoteUpdatedExecutor")
    private TaskExecutor emoteDeletedListenerExecutor;

    @Autowired
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onEmojiUpdateName(@NotNull EmojiUpdateNameEvent event) {
        if(listenerList == null) return;
        EmoteNameUpdatedModel model = getModel(event);
        listenerList.forEach(deletedListener -> listenerService.executeFeatureAwareListener(deletedListener, model, emoteDeletedListenerExecutor));
    }

    private EmoteNameUpdatedModel getModel(EmojiUpdateNameEvent event) {
        return EmoteNameUpdatedModel.builder().emote(event.getEmoji()).newValue(event.getNewValue()).oldValue(event.getOldValue()).build();
    }

}

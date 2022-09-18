package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.EmoteNameUpdatedModel;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class EmoteUpdatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<EmoteUpdatedListener> updatedListeners;

    @Autowired
    @Lazy
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onEmojiUpdateName(@NotNull EmojiUpdateNameEvent event) {
        if(updatedListeners == null) return;
        EmoteNameUpdatedModel model = getModel(event);
        updatedListeners.forEach(emoteUpdatedListener -> listenerService.executeFeatureAwareListener(emoteUpdatedListener, model));
    }

    private EmoteNameUpdatedModel getModel(EmojiUpdateNameEvent event) {
        return EmoteNameUpdatedModel.builder().emote(event.getEmoji()).newValue(event.getNewValue()).oldValue(event.getOldValue()).build();
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(updatedListeners);
    }
}

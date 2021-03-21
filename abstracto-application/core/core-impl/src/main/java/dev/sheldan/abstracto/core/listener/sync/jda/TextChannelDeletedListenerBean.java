package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.TextChannelDeletedModel;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class TextChannelDeletedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<TextChannelDeletedListener> listenerList;

    @Autowired
    private ListenerService listenerService;

    @Override
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent event) {
        if(listenerList == null) return;
        TextChannelDeletedModel model = getModel(event);
        listenerList.forEach(textChannelCreatedListener -> listenerService.executeFeatureAwareListener(textChannelCreatedListener, model));
    }

    private TextChannelDeletedModel getModel(TextChannelDeleteEvent event) {
        return TextChannelDeletedModel
                .builder()
                .channel(event.getChannel())
                .build();
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(listenerList);
    }
}

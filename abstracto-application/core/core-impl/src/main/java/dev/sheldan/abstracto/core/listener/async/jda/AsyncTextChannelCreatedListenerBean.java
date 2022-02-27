package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.TextChannelCreatedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

@Service
@Slf4j
public class AsyncTextChannelCreatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncTextChannelCreatedListener> listenerList;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("channelCreatedExecutor")
    private TaskExecutor channelCreatedExecutor;

    @Override
    public void onChannelCreate(@Nonnull ChannelCreateEvent event) {
        if(listenerList == null) return;
        TextChannelCreatedModel model = getModel(event);
        listenerList.forEach(textChannelCreatedListener -> listenerService.executeFeatureAwareListener(textChannelCreatedListener, model, channelCreatedExecutor));
    }

    private TextChannelCreatedModel getModel(ChannelCreateEvent event) {
        return TextChannelCreatedModel
                .builder()
                .channel(event.getChannel())
                .build();
    }

}

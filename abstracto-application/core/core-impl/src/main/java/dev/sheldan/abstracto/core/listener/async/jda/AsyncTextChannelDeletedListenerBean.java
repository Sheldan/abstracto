package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.TextChannelDeletedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

@Service
@Slf4j
public class AsyncTextChannelDeletedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncTextChannelDeletedListener> listenerList;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("channelDeletedExecutor")
    private TaskExecutor channelDeletedExecutor;


    @Override
    public void onChannelDelete(@Nonnull ChannelDeleteEvent event) {
        if(listenerList == null) return;
        TextChannelDeletedModel model = getModel(event);
        listenerList.forEach(textChannelCreatedListener -> listenerService.executeFeatureAwareListener(textChannelCreatedListener, model, channelDeletedExecutor));
    }

    private TextChannelDeletedModel getModel(ChannelDeleteEvent event) {
        return TextChannelDeletedModel
                .builder()
                .channel(event.getChannel())
                .build();
    }

}

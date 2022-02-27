package dev.sheldan.abstracto.core.listener.async.entity;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.AChannelDeletedListenerModel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Slf4j
public class AsyncAChannelDeletedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncAChannelDeletedListener> channelDeletedListeners;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    @Qualifier("aChannelDeletedExecutor")
    private TaskExecutor channelDeletedExecutor;

    @Autowired
    private AsyncAChannelDeletedListenerBean self;

    @Override
    public void onChannelDelete(@Nonnull ChannelDeleteEvent event) {
        self.deleteChannelInDb(event);
    }

    @Transactional
    public void deleteChannelInDb(ChannelDeleteEvent event) {
        channelManagementService.markAsDeleted(event.getChannel().getIdLong());
    }

    @TransactionalEventListener
    public void executeServerCreationListener(AChannelDeletedListenerModel model) {
        if(channelDeletedListeners == null) return;
        channelDeletedListeners.forEach(serverCreatedListener -> listenerService.executeListener(serverCreatedListener, model, channelDeletedExecutor));
    }

}

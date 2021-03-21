package dev.sheldan.abstracto.core.listener.async.entity;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.AChannelDeletedListenerModel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

    @Override
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent event) {
        channelManagementService.markAsDeleted(event.getChannel().getIdLong());
    }

    @TransactionalEventListener
    public void executeServerCreationListener(AChannelDeletedListenerModel model) {
        if(channelDeletedListeners == null) return;
        channelDeletedListeners.forEach(serverCreatedListener -> listenerService.executeListener(serverCreatedListener, model));
    }

}

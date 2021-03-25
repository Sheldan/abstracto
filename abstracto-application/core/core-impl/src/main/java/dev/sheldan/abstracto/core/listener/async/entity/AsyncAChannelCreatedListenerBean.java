package dev.sheldan.abstracto.core.listener.async.entity;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.database.AChannelType;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.listener.AChannelCreatedListenerModel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
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
public class AsyncAChannelCreatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncAChannelCreatedListener> channelListener;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    @Qualifier("aChannelCreatedExecutor")
    private TaskExecutor channelCreatedExecutor;

    @Autowired
    private AsyncAChannelCreatedListenerBean self;

    @Override
    public void onTextChannelCreate(@Nonnull TextChannelCreateEvent event) {
        self.createChannelInDatabase(event);
    }

    @Transactional
    public void createChannelInDatabase(@NotNull TextChannelCreateEvent event) {
        log.info("Creating text channel with ID {}.", event.getChannel().getIdLong());
        AServer serverObject = serverManagementService.loadOrCreate(event.getChannel().getGuild().getIdLong());
        TextChannel createdChannel = event.getChannel();
        AChannelType type = AChannelType.getAChannelType(createdChannel.getType());
        channelManagementService.createChannel(createdChannel.getIdLong(), type, serverObject);
    }

    @TransactionalEventListener
    public void executeServerCreationListener(AChannelCreatedListenerModel model) {
        if(channelListener == null) return;
        channelListener.forEach(serverCreatedListener -> listenerService.executeListener(serverCreatedListener, model, channelCreatedExecutor));
    }

}

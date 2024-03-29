package dev.sheldan.abstracto.core.listener.async.entity;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.database.AChannelType;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.listener.AChannelCreatedListenerModel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
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
    public void onChannelCreate(@Nonnull ChannelCreateEvent event) {
        self.createChannelInDatabase(event);
    }

    @Transactional
    public void createChannelInDatabase(ChannelCreateEvent event) {
        log.info("Creating text channel with ID {}.", event.getChannel().getIdLong());
        if(event.getChannel() instanceof GuildChannel) {
            AServer serverObject = serverManagementService.loadOrCreate(((GuildChannel)event.getChannel()).getGuild().getIdLong());
            Channel createdChannel = event.getChannel();
            AChannelType type = AChannelType.getAChannelType(createdChannel.getType());
            channelManagementService.createChannel(createdChannel.getIdLong(), type, serverObject);
        } else {
            log.info("Guild independent channel created - doing nothing.");
        }
    }

    @TransactionalEventListener
    public void executeServerCreationListener(AChannelCreatedListenerModel model) {
        if(channelListener == null) return;
        channelListener.forEach(serverCreatedListener -> listenerService.executeListener(serverCreatedListener, model, channelCreatedExecutor));
    }

}

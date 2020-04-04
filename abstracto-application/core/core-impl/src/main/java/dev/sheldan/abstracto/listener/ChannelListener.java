package dev.sheldan.abstracto.listener;

import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.models.AChannelType;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.ServerRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

@Service
@Slf4j
public class ChannelListener extends ListenerAdapter {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    @Transactional
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent event) {
        log.info("Handling channel delete event. Channel {}, Server {}", event.getChannel().getIdLong(), event.getGuild().getIdLong());
        channelManagementService.markAsDeleted(event.getChannel().getIdLong());
    }

    @Override
    @Transactional
    public void onTextChannelCreate(@Nonnull TextChannelCreateEvent event) {
        log.info("Handling channel created event. Channel {}, Server {}", event.getChannel().getIdLong(), event.getGuild().getIdLong());
        AServer serverObject = serverRepository.getOne(event.getGuild().getIdLong());
        TextChannel createdChannel = event.getChannel();
        AChannelType type = AChannel.getAChannelType(createdChannel.getType());
        AChannel newChannel = channelManagementService.createChannel(createdChannel.getIdLong(), type);
        serverManagementService.addChannelToServer(serverObject, newChannel);
    }
}

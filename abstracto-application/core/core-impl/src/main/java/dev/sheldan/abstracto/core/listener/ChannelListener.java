package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.models.database.AChannelType;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
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
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    @Transactional
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent event) {
        log.info("Handling channel delete event. Marking channel {} as deleted in server {}", event.getChannel().getIdLong(), event.getGuild().getIdLong());
        channelManagementService.markAsDeleted(event.getChannel().getIdLong());
    }

    @Override
    @Transactional
    public void onTextChannelCreate(@Nonnull TextChannelCreateEvent event) {
        log.info("Handling channel created event. Creating channel {} in server {}", event.getChannel().getIdLong(), event.getGuild().getIdLong());
        AServer serverObject = serverManagementService.loadOrCreate(event.getGuild().getIdLong());
        TextChannel createdChannel = event.getChannel();
        AChannelType type = AChannelType.getAChannelType(createdChannel.getType());
        channelManagementService.createChannel(createdChannel.getIdLong(), type, serverObject);
    }
}

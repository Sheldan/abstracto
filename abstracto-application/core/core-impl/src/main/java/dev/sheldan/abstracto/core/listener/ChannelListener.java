package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AChannelType;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.models.utils.ChannelUtils;
import dev.sheldan.abstracto.core.repository.ServerRepository;
import dev.sheldan.abstracto.core.service.management.ChannelManagementServiceBean;
import dev.sheldan.abstracto.core.service.management.ServerManagementServiceBean;
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
    private ChannelManagementServiceBean channelManagementService;

    @Autowired
    private ServerManagementServiceBean serverManagementService;

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
        TextChannel createdChannel = event.getChannel();
        AChannelType type = ChannelUtils.getAChannelType(createdChannel.getType());
        ChannelDto newChannel = channelManagementService.createChannel(createdChannel.getIdLong(), type);
        ServerDto serverDto = ServerDto.builder().id(event.getGuild().getIdLong()).build();
        serverManagementService.addChannelToServer(serverDto, newChannel);
    }
}

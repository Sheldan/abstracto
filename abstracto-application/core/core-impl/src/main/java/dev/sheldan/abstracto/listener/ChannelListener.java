package dev.sheldan.abstracto.listener;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.repository.ServerRepository;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Optional;

@Service
public class ChannelListener extends ListenerAdapter {

    @Autowired
    private ServerRepository serverRepository;

    private static Logger logger = LoggerFactory.getLogger(ChannelListener.class);

    @Override
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent event) {
        AServer serverObject = serverRepository.getOne(event.getGuild().getIdLong());
        serverObject.getChannels().add(AChannel.builder().id(event.getChannel().getIdLong()).build());
    }

    @Override
    public void onTextChannelCreate(@Nonnull TextChannelCreateEvent event) {
        AServer serverObject = serverRepository.getOne(event.getGuild().getIdLong());
        TextChannel createdChannel = event.getChannel();
        Optional<AChannel> possibleChannel = serverObject.getChannels().stream().filter(aChannel -> aChannel.id == createdChannel.getIdLong()).findAny();
        if(possibleChannel.isPresent()){
            serverObject.getChannels().remove(possibleChannel.get());
            logger.info("Adding channel {} with id {}", createdChannel.getName(), createdChannel.getIdLong());
        } else {
            logger.warn("Channel removed event for channel which was not in present");
        }
    }
}

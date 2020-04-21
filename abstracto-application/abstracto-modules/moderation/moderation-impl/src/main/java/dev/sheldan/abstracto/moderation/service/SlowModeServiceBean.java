package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.exception.ChannelException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.BotService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
public class SlowModeServiceBean implements SlowModeService {

    @Autowired
    private BotService botService;

    @Override
    public void setSlowMode(TextChannel channel, Duration duration) {
        log.info("Setting slow mode to {} in channel {} in server {}", duration.toString(), channel.getIdLong(), channel.getGuild().getId());
        long seconds = duration.getSeconds();
        if(seconds > TextChannel.MAX_SLOWMODE) {
            throw new IllegalArgumentException("Slow mode duration must be < " + TextChannel.MAX_SLOWMODE + " seconds.");
        }
        channel.getManager().setSlowmode((int) seconds).queue();
    }

    @Override
    public void setSlowMode(AChannel channel, Duration duration)  {
        Optional<TextChannel> textChannelOptional = botService.getTextChannelFromServer(channel.getServer().getId(), channel.getId());
        if(textChannelOptional.isPresent()) {
            TextChannel textChannel = textChannelOptional.get();
            this.setSlowMode(textChannel, duration);
        } else {
            throw new ChannelException(String.format("Channel %s not found in guild %s", channel.getId(), channel.getServer().getId()));
        }
    }
}

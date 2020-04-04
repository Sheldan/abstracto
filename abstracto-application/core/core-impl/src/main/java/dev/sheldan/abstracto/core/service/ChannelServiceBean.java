package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.ChannelException;
import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChannelServiceBean implements ChannelService {

    @Autowired
    private Bot botService;

    @Override
    public void sendTextInAChannel(String text, AChannel channel) {
        Guild guild = botService.getInstance().getGuildById(channel.getServer().getId());
        if (guild != null) {
            TextChannel textChannel = guild.getTextChannelById(channel.getId());
            if(textChannel != null) {
                textChannel.sendMessage(text).queue();
            } else {
                log.error("Channel {} to post towards was not found in server {}", channel.getId(), channel.getServer().getId());
                throw new ChannelException(String.format("Channel %s to post to not found.", channel.getId()));
            }
        } else {
            log.error("Guild {} was not found when trying to post a message", channel.getServer().getId());
            throw new GuildException(String.format("Guild %s to post in channel %s was not found.", channel.getServer().getId(), channel.getId()));
        }
    }
}

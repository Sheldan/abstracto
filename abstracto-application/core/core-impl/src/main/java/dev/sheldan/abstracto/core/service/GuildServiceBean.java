package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GuildServiceBean implements GuildService {

    @Autowired
    private BotService botService;

    @Override
    public Optional<Guild> getGuildByIdOptional(Long serverId) {
        return Optional.ofNullable(botService.getInstance().getGuildById(serverId));
    }

    @Override
    public Guild getGuildById(Long serverId) {
        Guild guildById = botService.getInstance().getGuildById(serverId);
        if(guildById == null) {
            throw new GuildNotFoundException(serverId);
        }
        return guildById;
    }
}

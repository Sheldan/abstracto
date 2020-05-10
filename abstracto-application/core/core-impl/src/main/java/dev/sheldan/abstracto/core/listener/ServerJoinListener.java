package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class ServerJoinListener extends ListenerAdapter {

    @Autowired
    private List<ServerConfigListener> configListeners;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    @Transactional
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        AServer server = serverManagementService.loadOrCreate(event.getGuild().getIdLong());
        configListeners.forEach(serverConfigListener -> serverConfigListener.updateServerConfig(server));
    }
}

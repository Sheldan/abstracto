package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.listener.sync.entity.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Slf4j
public class ServerJoinListener extends ListenerAdapter {

    @Autowired(required = false)
    private List<ServerConfigListener> configListeners;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ServerJoinListener self;

    @Override
    @Transactional
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        if(configListeners == null) return;
        log.info("Joining guild {}, executing server config listener.", event.getGuild().getId());
        AServer server = serverManagementService.loadOrCreate(event.getGuild().getIdLong());
        configListeners.forEach(serverConfigListener -> self.executingIndividualServerConfigListener(server, serverConfigListener));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executingIndividualServerConfigListener(AServer server, ServerConfigListener serverConfigListener) {
        log.trace("Executing server config listener for server {}.", server.getId());
        serverConfigListener.updateServerConfig(server);
    }
}

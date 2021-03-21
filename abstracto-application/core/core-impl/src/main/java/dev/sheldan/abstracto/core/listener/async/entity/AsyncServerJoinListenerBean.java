package dev.sheldan.abstracto.core.listener.async.entity;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.ServerCreatedListenerModel;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
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
public class AsyncServerJoinListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncServerCreatedListener> serverCreatedListeners;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("serverJoinExecutor")
    private TaskExecutor serverJoinExecutor;

    @Override
    @Transactional
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        if(serverCreatedListeners == null) return;
        log.info("Joining guild {}, creating server.", event.getGuild().getId());
        serverManagementService.loadOrCreate(event.getGuild().getIdLong());
   }

    @TransactionalEventListener
    public void executeServerCreationListener(ServerCreatedListenerModel model) {
        if(serverCreatedListeners == null) return;
        serverCreatedListeners.forEach(asyncServerCreatedListener -> listenerService.executeListener(asyncServerCreatedListener, model, serverJoinExecutor));
    }

}

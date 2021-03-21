package dev.sheldan.abstracto.core.listener.async.entity;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.listener.ARoleCreatedListenerModel;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Slf4j
public class AsyncARoleCreatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncARoleCreatedListener> roleCreatedListeners;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        AServer server = serverManagementService.loadServer(event.getGuild());
        roleManagementService.createRole(event.getRole().getIdLong(), server);
    }

    @TransactionalEventListener
    public void executeServerCreationListener(ARoleCreatedListenerModel model) {
        if(roleCreatedListeners == null) return;
        roleCreatedListeners.forEach(asyncServerCreatedListener -> listenerService.executeListener(asyncServerCreatedListener, model));
    }

}

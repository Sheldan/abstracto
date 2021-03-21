package dev.sheldan.abstracto.core.listener.async.entity;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.ARoleDeletedListenerModel;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Slf4j
public class AsyncARoleDeletedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncARoleDeletedListener> roleDeletedListeners;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("aRoleDeletedExecutor")
    private TaskExecutor roleDeletedExecutor;

    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        roleManagementService.markDeleted(event.getRole().getIdLong());
    }

    @TransactionalEventListener
    public void executeServerCreationListener(ARoleDeletedListenerModel model) {
        if(roleDeletedListeners == null) return;
        roleDeletedListeners.forEach(serverCreatedListener -> listenerService.executeListener(serverCreatedListener, model, roleDeletedExecutor));
    }

}

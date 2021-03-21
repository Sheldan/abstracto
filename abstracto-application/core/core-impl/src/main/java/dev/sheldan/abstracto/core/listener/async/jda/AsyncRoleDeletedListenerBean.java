package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.RoleDeletedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Slf4j
public class AsyncRoleDeletedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncRoleDeletedListener> listenerList;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("roleDeletedExecutor")
    private TaskExecutor roleDeletedExecutor;

    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        if(listenerList == null) return;
        RoleDeletedModel model = getModel(event);
        listenerList.forEach(roleCreatedListener -> listenerService.executeFeatureAwareListener(roleCreatedListener, model, roleDeletedExecutor));
    }

    private RoleDeletedModel getModel(RoleDeleteEvent event) {
        return RoleDeletedModel
                .builder()
                .role(event.getRole())
                .build();
    }

}

package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.RoleCreatedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Slf4j
public class AsyncRoleCreatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncRoleCreatedListener> listenerList;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("roleCreatedExecutor")
    private TaskExecutor roleCreatedExecutor;


    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        if(listenerList == null) return;
        RoleCreatedModel model = getModel(event);
        listenerList.forEach(roleCreatedListener -> listenerService.executeFeatureAwareListener(roleCreatedListener, model, roleCreatedExecutor));
    }

    private RoleCreatedModel getModel(RoleCreateEvent event) {
        return RoleCreatedModel
                .builder()
                .role(event.getRole())
                .build();
    }


}

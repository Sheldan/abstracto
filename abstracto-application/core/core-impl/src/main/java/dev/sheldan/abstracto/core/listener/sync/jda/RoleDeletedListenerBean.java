package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.RoleDeletedModel;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Slf4j
public class RoleDeletedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<RoleDeletedListener> listenerList;

    @Autowired
    private ListenerService listenerService;

    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        if(listenerList == null) return;
        RoleDeletedModel model = getModel(event);
        listenerList.forEach(roleCreatedListener -> listenerService.executeFeatureAwareListener(roleCreatedListener, model));
    }

    private RoleDeletedModel getModel(RoleDeleteEvent event) {
        return RoleDeletedModel
                .builder()
                .role(event.getRole())
                .build();
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(listenerList);
    }
}

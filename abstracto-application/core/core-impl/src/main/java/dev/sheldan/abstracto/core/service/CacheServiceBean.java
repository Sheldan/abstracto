package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.listener.async.entity.AsyncCacheClearingListener;
import dev.sheldan.abstracto.core.models.listener.VoidListenerModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CacheServiceBean {

    @Autowired(required = false)
    private List<AsyncCacheClearingListener> listeners;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("cacheClearedExecutor")
    private TaskExecutor cacheClearedExecutor;

    public void clearCaches() {
        if(listeners == null) return;
        log.info("Executing {} cache cleared listeners.", listeners.size());
        VoidListenerModel model = VoidListenerModel.builder().build();
        listeners.forEach(asyncCacheClearingListener ->
                listenerService.executeListener(asyncCacheClearingListener, model, cacheClearedExecutor));
    }
}

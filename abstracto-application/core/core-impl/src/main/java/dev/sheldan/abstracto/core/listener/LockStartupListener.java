package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.service.LockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LockStartupListener {

    @Autowired
    private LockService lockService;

    @EventListener
    @Transactional
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxStartEvt) {
        lockService.setupLocks();
    }
}

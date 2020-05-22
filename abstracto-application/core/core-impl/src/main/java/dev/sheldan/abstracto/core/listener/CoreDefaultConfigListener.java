package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CoreDefaultConfigListener  {

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;


    @Value("${abstracto.prefix}")
    private String prefix;

    @EventListener
    @Transactional
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxStartEvt) {
        defaultConfigManagementService.createDefaultConfig("prefix", prefix);
    }
}

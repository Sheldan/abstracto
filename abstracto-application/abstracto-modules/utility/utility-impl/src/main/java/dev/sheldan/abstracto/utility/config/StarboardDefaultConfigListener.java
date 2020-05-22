package dev.sheldan.abstracto.utility.config;

import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StarboardDefaultConfigListener {

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Autowired
    private StarboardConfig starboardConfig;

    @EventListener
    @Transactional
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxStartEvt) {
        for (int i = 0; i < starboardConfig.getLvl().size(); i++) {
            Integer value = starboardConfig.getLvl().get(i);
            defaultConfigManagementService.createDefaultConfig("starLvl" + ( i + 1 ), Long.valueOf(value));
        }
    }
}

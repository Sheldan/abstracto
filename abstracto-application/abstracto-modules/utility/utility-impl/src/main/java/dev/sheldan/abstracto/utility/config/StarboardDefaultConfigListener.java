package dev.sheldan.abstracto.utility.config;

import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.utility.service.StarboardServiceBean;
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
        int levels = starboardConfig.getLvl().size();
        for (int i = 0; i < levels; i++) {
            Integer value = starboardConfig.getLvl().get(i);
            defaultConfigManagementService.createDefaultConfig(StarboardServiceBean.STAR_LVL_CONFIG_PREFIX + ( i + 1 ), Long.valueOf(value));
        }
    }
}
